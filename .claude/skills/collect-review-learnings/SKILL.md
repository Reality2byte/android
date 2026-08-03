---
name: collect-review-learnings
description: >
  Use when the user wants to harvest recent GitLab MR review discussions and turn reviewer
  feedback into improvements to the project's code-review knowledge files — e.g. "collect the
  latest MR reviews", "update the review skill from recent MRs", "what have reviewers been
  flagging lately", or a regular /collect-review-learnings run.
triggers:
  - /collect-review-learnings
  - collect review learnings
  - update review skill from MR discussions
---

# Collect Review Learnings

Collect recent MR review discussions from GitLab, assess which reviewer feedback is
generalizable review knowledge, and — after the user confirms — write the approved items
into the project's code-review knowledge files.

## Usage

```
/collect-review-learnings                       # last 14 days, project from env
/collect-review-learnings --days 30             # custom window
/collect-review-learnings --project <url|path>  # different GitLab project
/collect-review-learnings --mr 12345            # single MR only (spot run / verification)
/collect-review-learnings auto                  # skip confirmation; apply high-confidence items only
```

## Configuration

> ⚠️ This repository mirrors publicly. Never hardcode the internal GitLab hostname (or any
> internal identifier) in this file, in commands you save anywhere, or in the knowledge-file
> edits you produce. All examples below use `gitlab.example.com` placeholders.

Resolve the target project in this order:

1. `--project` argument — a full URL, or a `group/project` path (path form takes its host
   from the env variable below).
2. Environment variable `MEGA_GITLAB_PROJECT_URL`, e.g. `https://gitlab.example.com/group/project`.

Derive the variables used by every command below:

```bash
PROJECT_URL=${MEGA_GITLAB_PROJECT_URL:?set MEGA_GITLAB_PROJECT_URL or pass --project}
API_BASE="$(printf '%s' "$PROJECT_URL" | sed -E 's#^(https?://[^/]+)/.*#\1#')/api/v4"
PROJECT="$(printf '%s' "$PROJECT_URL" | sed -E 's#^https?://[^/]+/##; s#/#%2F#g')"
```

If neither is present, **stop** and ask the user to set it (gitignored), e.g. in
`.claude/settings.local.json`:

```json
{ "env": { "MEGA_GITLAB_PROJECT_URL": "https://gitlab.example.com/group/project" } }
```

**Token**: read from `~/.config/mega-mr/gitlab-token`, always inline —
`-H "PRIVATE-TOKEN: $(cat ~/.config/mega-mr/gitlab-token)"`. Never print or echo it.
If the file is missing, stop and tell the user:

```bash
mkdir -p ~/.config/mega-mr
printf '%s' '<gitlab-personal-access-token>' > ~/.config/mega-mr/gitlab-token
chmod 600 ~/.config/mega-mr/gitlab-token   # token needs read_api (or api) scope
```

## Step 1 — Collect discussions

When `--mr <iid>` was given, skip the window listing — but still fetch the MR's metadata
(`GET $API_BASE/projects/$PROJECT/merge_requests/$IID`) for the `title`, `web_url`, and
`author` the report needs, then go straight to the discussions call.

```bash
# MRs with any activity in the window (all states — merged MRs carry the settled reviews)
UPDATED_AFTER=$(python3 -c "import datetime;print((datetime.datetime.now(datetime.timezone.utc)-datetime.timedelta(days=${DAYS:-14})).strftime('%Y-%m-%dT%H:%M:%SZ'))")
curl -sf -H "PRIVATE-TOKEN: $(cat ~/.config/mega-mr/gitlab-token)" \
  "$API_BASE/projects/$PROJECT/merge_requests?updated_after=$UPDATED_AFTER&state=all&per_page=100&page=1"
```
Page until a page returns fewer than 100 items (cap 10 pages). Keep `iid`, `title`,
`web_url`, `author.username`, `state`.

For each MR, fetch **discussions** (threads with file positions — not flat notes) and
pre-filter to substantive human review threads in one pass:

```bash
curl -sf -H "PRIVATE-TOKEN: $(cat ~/.config/mega-mr/gitlab-token)" \
  "$API_BASE/projects/$PROJECT/merge_requests/$IID/discussions?per_page=100" \
| jq '[.[]
    | {resolved: ([.notes[].resolved] | any),
       notes: [.notes[]
         | select(.system | not)
         | select(.author.username | test("bot|appdev|jenkins|webautodeploy"; "i") | not)
         | {author: .author.username, path: .position.new_path, body}]}
    | select([.notes[].body]
        | any(test("^(lgtm|done|fixed|thanks|\\+1|👍|deliver_\\w+|upload_\\w+|postRelease|build sdk-aar|jenkins rebuild)"; "i") | not))]'
```
Page if a page comes back full. Filter rules, verified against this project's data:
- `system: true` notes are never review feedback.
- Bot authors: `appdev` (CI review bot), `jenkins`, `webautodeploy`, any name containing `bot`.
- The trivial/noise test applies at **thread level**: a thread survives only if at least one
  note is more than a short ack (`LGTM`, `Done`, …) or a human-posted **CI trigger command**
  (`deliver_qa`, `deliver_appStore`, `upload_symbol`, `postRelease`, `build sdk-aar`,
  `jenkins rebuild`, …). A surviving thread keeps **all** its human notes — a bare "Done"
  from the author inside a real review thread is adoption evidence, not noise.
- Threads may predate the window (the MR was merely active in it). Keep them: a settled
  thread is the best evidence.

If a discussions fetch fails for one MR, record it and continue — it must appear in the
report as *could not analyze*, never silently vanish.

## Step 2 — Assess

**Scale switch**: count MRs that still carry threads after filtering. **≤ 10** → assess
inline. **More** → dispatch subagents in batches of 5–8 MRs; give each batch the filtered
thread JSON and the three questions below, and have each return structured candidates:
`{rule, evidence: [{mr_url, quote, author}], occurrences, target_file, confidence: high|medium|low}`.
Merge and dedup the batches yourself before reporting.

Judge every thread on three questions:

1. **Generalizable?** A reusable review rule (architecture, convention, recurring bug
   pattern) — or a one-off remark about this ticket only? Discard one-offs, factual
   Q&A, and release chatter.
2. **Already covered?** Check the existing knowledge files for an equivalent entry —
   covered items are skipped. (This is also what makes repeated runs idempotent: rules
   applied last run are recognized as covered this run.) A thread that shows an existing
   rule is *incomplete* — reviewers asking a question the file doesn't answer — is a
   candidate to **extend** that entry, and worth surfacing.
3. **Where does it belong?**

   | Feedback topic | Target file |
   |---|---|
   | ViewModel (tests) | `.claude/skills/viewmodel/viewmodel-conventions.md` (`viewmodel-test-conventions.md`) |
   | UseCase (tests) | `.claude/skills/usecase/usecase-conventions.md` (`usecase-test-conventions.md`) |
   | Mapper (tests) | `.claude/skills/mapper/mapper-conventions.md` (`mapper-test-conventions.md`) |
   | Compose / UI state / theming | `android-code-review/SKILL.md` §9 |
   | Navigation 3 | `android-code-review/SKILL.md` §10 + `navigation/CLAUDE.md` |
   | Room / data layer | `android-code-review/SKILL.md` §11 + `data/CLAUDE.md` |
   | Any general review dimension | the matching section of `.claude/skills/android-code-review/SKILL.md` |
   | Specific to one module | that module's `CLAUDE.md` |

Evidence rules:
- Quote the reviewer verbatim and link the MR. Count occurrences across MRs — a rule
  flagged in several MRs outranks a single mention.
- Bot review comments are **not** evidence, but an author's reply visibly adopting review
  feedback ("moved X inside the mutex", a follow-up commit) **is** — it shows the rule
  survived human judgment.
- Disagreement matters: if the author pushed back and the reviewer conceded, the thread
  teaches the *exception*, not the rule. When a thread ends in disagreement or it is
  unclear which side landed, check the merged code (local git or the project's diffs API) —
  what actually merged decides what the thread teaches. Exception: an **accepted-but-deferred**
  fix ("I'll solve it in the next MR") still teaches the rule — deferral is not push-back,
  even though the merged code contradicts it.
- Confidence: `high` = flagged in multiple MRs, or a single thread whose adoption is
  visible in the merged code; `medium` = accepted but unverified, or deferred; `low` =
  unresolved, no clear outcome. (Spot runs on one MR rarely exceed `medium` for
  single-mention rules — that is intended.)

## Step 3 — Report and confirm

Open with a scope line — the window and project scanned, or for spot runs the MR title,
author, and state — then one entry per candidate:

```markdown
### <n>. <one-sentence rule>  (confidence: high|medium|low)
- **Evidence**: [MR !1234](url) — @reviewer: "<quoted comment>"  (+ further MRs, occurrence count)
- **Target**: `<file>` → <section>
- **Proposed edit**: the drafted checklist bullet — and, when the target file uses them, a ❌/✅ code example
```

Close the report with: MRs scanned / MRs with review threads / threads assessed / candidates,
plus any *could not analyze* MRs, and — when nothing qualified — say so explicitly and stop.

Then let the user approve or reject **per item** (`AskUserQuestion` with multiSelect, or
free-form). Only with the `auto` argument skip confirmation and apply high-confidence
items only.

## Step 4 — Apply

For each approved item, edit the target file **matching its existing format exactly**
(checklist bullet style, ❌/✅ `Common Issues` examples, comment density). Extend an
existing entry rather than adding a near-duplicate. Nothing written may contain internal
hostnames or identifiers.

Finish by listing the modified files and remind the user the changes are on the current
branch — `/create-mr` when they're ready to share.

## Edge cases

- Missing token or project URL → stop with the setup instructions above.
- MR-list call fails → stop and report; never assess a partial window silently.
- Per-MR fetch fails → continue, flag as *could not analyze* in the report.
- Empty window / no candidates → report the counts and end; do not pad findings.
