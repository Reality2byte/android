# Test placement: AAT (E2E) vs in-code (`:data-test`)

Quick rules for app engineers deciding where a test goes. Full policy and the
living removable-tests list live on Confluence (ATT space):
<https://confluence.developers.mega.co.nz/pages/viewpage.action?pageId=175089566>

## Default

A new **UI / app-logic regression test → write it in-code** on `:data-test`.
It runs on every MR, is deterministic, and needs no device farm or real account.
Reserve the **AAT** Appium E2E suite for coverage only a real device + real
backend can give — the AAT team is small, so spend their capacity there.

## Write it in-code (`:data-test`) when ALL hold

- Its value is **UI / app logic reacting to SDK state or events**, or that the
  app **invokes the right gateway method** with the right arguments.
- The screen reaches the SDK **through** `MegaApiGateway` / `MegaChatApiGateway`.
  If it still calls the SDK directly, refactor it behind the gateway first —
  until then the fake cannot intercept it.
- It needs **no** real network, real bytes, second device, push, call media, or
  hardware to be meaningful.

Reference example: `app/src/androidTest/.../CloudDriveUploadTest.kt`.
State-changing ops (rename / move / copy / delete) don't mutate the fake tree on
their own — use the `FakeNodeTree` mutating-op helper (AND-24499).

## Keep it in AAT when its value depends on

- Real backend semantics, real bytes, or real artifacts (files on disk, offline
  copies, real share links, thumbnails, throughput/CRC).
- A real second account or device; real push / FCM; real call / WebRTC media.
- Device hardware (camera, mic, GPS, OS permission dialogs, cross-app share sheet).
- Performance / timing; services not behind the SDK gateways (VPN, Password Manager).

## Removing an AAT test

Only when **all** hold: the scenario is fully **Portable** (not "Partial"); an
in-code equivalent has **landed on `develop` and is green in CI**, asserting the
same user-visible outcome; and removing it drops no coverage held only in AAT.
**Never** remove a "Not-portable" scenario. A "Partial" scenario's in-code port
is *additive* (a faster regression signal), not a replacement — keep both.
