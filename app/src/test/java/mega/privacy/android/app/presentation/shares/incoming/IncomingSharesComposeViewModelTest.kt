package mega.privacy.android.app.presentation.shares.incoming

import android.view.MenuItem
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import de.palm.composestateevents.StateEvent
import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.StateEventWithContentConsumed
import de.palm.composestateevents.StateEventWithContentTriggered
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mega.privacy.android.app.presentation.clouddrive.OptionItems
import mega.privacy.android.app.presentation.data.NodeUIItem
import mega.privacy.android.app.presentation.mapper.HandleOptionClickMapper
import mega.privacy.android.app.presentation.mapper.OptionsItemInfo
import mega.privacy.android.app.presentation.time.mapper.DurationInSecondsTextMapper
import mega.privacy.android.app.presentation.transfers.starttransfer.model.TransferTriggerEvent
import mega.privacy.android.core.test.extension.CoroutineMainDispatcherExtension
import mega.privacy.android.data.mapper.FileDurationMapper
import mega.privacy.android.domain.entity.SortOrder
import mega.privacy.android.domain.entity.node.Node
import mega.privacy.android.domain.entity.node.NodeChanges
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.entity.node.NodeUpdate
import mega.privacy.android.domain.entity.node.TypedFolderNode
import mega.privacy.android.domain.entity.node.shares.ShareFileNode
import mega.privacy.android.domain.entity.node.shares.ShareFolderNode
import mega.privacy.android.domain.entity.preference.ViewType
import mega.privacy.android.domain.usecase.GetCloudSortOrder
import mega.privacy.android.domain.usecase.GetNodeByIdUseCase
import mega.privacy.android.domain.usecase.GetOthersSortOrder
import mega.privacy.android.domain.usecase.GetParentNodeUseCase
import mega.privacy.android.domain.usecase.GetRootNodeUseCase
import mega.privacy.android.domain.usecase.MonitorContactUpdates
import mega.privacy.android.domain.usecase.account.MonitorRefreshSessionUseCase
import mega.privacy.android.domain.usecase.contact.AreCredentialsVerifiedUseCase
import mega.privacy.android.domain.usecase.contact.GetContactVerificationWarningUseCase
import mega.privacy.android.domain.usecase.network.MonitorConnectivityUseCase
import mega.privacy.android.domain.usecase.node.IsNodeInRubbishBinUseCase
import mega.privacy.android.domain.usecase.node.MonitorNodeUpdatesUseCase
import mega.privacy.android.domain.usecase.offline.MonitorOfflineNodeUpdatesUseCase
import mega.privacy.android.domain.usecase.shares.GetIncomingShareParentUserEmailUseCase
import mega.privacy.android.domain.usecase.shares.GetIncomingSharesChildrenNodeUseCase
import mega.privacy.android.domain.usecase.viewtype.MonitorViewType
import mega.privacy.android.domain.usecase.viewtype.SetViewType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.seconds

@ExtendWith(CoroutineMainDispatcherExtension::class)
@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IncomingSharesComposeViewModelTest {
    private lateinit var underTest: IncomingSharesComposeViewModel

    private val getRootNodeUseCase = mock<GetRootNodeUseCase>()
    private val isNodeInRubbishBinUseCase = mock<IsNodeInRubbishBinUseCase>()
    private val monitorNodeUpdatesFakeFlow = MutableSharedFlow<NodeUpdate>()
    private val monitorNodeUpdatesUseCase = mock<MonitorNodeUpdatesUseCase>()
    private val getParentNodeUseCase = mock<GetParentNodeUseCase>()
    private val getIncomingSharesChildrenNodeUseCase = mock<GetIncomingSharesChildrenNodeUseCase>()
    private val getCloudSortOrder = mock<GetCloudSortOrder>()
    private val handleOptionClickMapper = mock<HandleOptionClickMapper>()
    private val monitorViewType = mock<MonitorViewType>()
    private val setViewType = mock<SetViewType>()
    private val monitorRefreshSessionUseCase = mock<MonitorRefreshSessionUseCase>()
    private val fileDurationMapper = mock<FileDurationMapper>()
    private val monitorOfflineNodeUpdatesUseCase = mock<MonitorOfflineNodeUpdatesUseCase>()
    private val monitorConnectivityUseCase = mock<MonitorConnectivityUseCase>()
    private val durationInSecondsTextMapper = mock<DurationInSecondsTextMapper>()
    private val monitorContactUpdatesUseCase = mock<MonitorContactUpdates>()
    private val getOthersSortOrder = mock<GetOthersSortOrder>()
    private val getContactVerificationWarningUseCase = mock<GetContactVerificationWarningUseCase>()
    private val areCredentialsVerifiedUseCase = mock<AreCredentialsVerifiedUseCase>()
    private val getNodeByIdUseCase = mock<GetNodeByIdUseCase>()
    private val getIncomingShareParentUserEmailUseCase =
        mock<GetIncomingShareParentUserEmailUseCase>()

    @BeforeEach
    fun setUp() {
        runBlocking {
            stubCommon()
        }
        initViewModel()
    }

    private fun initViewModel() {
        underTest = IncomingSharesComposeViewModel(
            getRootNodeUseCase = getRootNodeUseCase,
            monitorNodeUpdatesUseCase = monitorNodeUpdatesUseCase,
            getParentNodeUseCase = getParentNodeUseCase,
            isNodeInRubbishBinUseCase = isNodeInRubbishBinUseCase,
            getIncomingSharesChildrenNodeUseCase = getIncomingSharesChildrenNodeUseCase,
            getCloudSortOrder = getCloudSortOrder,
            setViewType = setViewType,
            monitorViewType = monitorViewType,
            handleOptionClickMapper = handleOptionClickMapper,
            monitorRefreshSessionUseCase = monitorRefreshSessionUseCase,
            fileDurationMapper = fileDurationMapper,
            monitorOfflineNodeUpdatesUseCase = monitorOfflineNodeUpdatesUseCase,
            monitorConnectivityUseCase = monitorConnectivityUseCase,
            durationInSecondsTextMapper = durationInSecondsTextMapper,
            monitorContactUpdatesUseCase = monitorContactUpdatesUseCase,
            getOthersSortOrder = getOthersSortOrder,
            getContactVerificationWarningUseCase = getContactVerificationWarningUseCase,
            areCredentialsVerifiedUseCase = areCredentialsVerifiedUseCase,
            getIncomingShareParentUserEmailUseCase = getIncomingShareParentUserEmailUseCase,
            getNodeByIdUseCase = getNodeByIdUseCase
        )
    }

    @Test
    fun `test that initial state is returned`() = runTest {
        underTest.state.test {
            val initial = awaitItem()
            assertThat(initial.currentViewType).isEqualTo(ViewType.LIST)
            assertThat(initial.currentHandle).isEqualTo(-1L)
            assertThat(initial.nodesList).isEmpty()
            assertThat(initial.totalSelectedFolderNodes).isEqualTo(0)
            assertThat(initial.totalSelectedFileNodes).isEqualTo(0)
            assertThat(initial.selectedNodeHandles).isEmpty()
            assertThat(initial.isPendingRefresh).isFalse()
            assertThat(initial.isInSelection).isFalse()
            assertThat(initial.isAccessedFolderExited).isFalse()
            assertThat(initial.accessedFolderHandle).isNull()
            assertThat(initial.sortOrder).isEqualTo(SortOrder.ORDER_NONE)
            assertThat(initial.optionsItemInfo).isNull()
            assertThat(initial.isConnected).isFalse()
            assertThat(initial.downloadEvent).isInstanceOf(StateEventWithContent::class.java)
            assertThat(initial.updateToolbarTitleEvent).isInstanceOf(StateEvent::class.java)
            assertThat(initial.exitIncomingSharesEvent).isInstanceOf(StateEvent::class.java)
            assertThat(initial.openedFolderNodeHandles).isEmpty()
        }
    }

    @Test
    fun `test that the file browser handle is updated if new value provided`() = runTest {
        underTest.state.drop(1) // Skip the initial emission
            .map { it.currentHandle }
            .distinctUntilChanged()
            .test {
                val newValue = 123456789L
                underTest.setCurrentHandle(newValue)
                assertThat(awaitItem()).isEqualTo(newValue)
            }
    }

    @Test
    fun `test that the file browser handle is set`() =
        runTest {
            val expectedHandle = 123456789L
            underTest.setCurrentHandle(expectedHandle)
            assertThat(underTest.getCurrentNodeHandle()).isEqualTo(expectedHandle)
        }

    @Test
    fun `test that the node name is updated when current node is updated`() =
        runTest {
            val expectedHandle = 123456789L
            val expectedName = "node name"
            val node = mock<TypedFolderNode> {
                on { name }.thenReturn(expectedName)
            }
            whenever(getNodeByIdUseCase.invoke(NodeId(expectedHandle))).thenReturn(node)
            underTest.setCurrentHandle(expectedHandle)
            assertThat(underTest.getCurrentNodeHandle()).isEqualTo(expectedHandle)
            assertThat(underTest.state.value.currentNodeName).isEqualTo(expectedName)
        }

    @Test
    fun `test that the nodes are returned when setting the file browser handle`() =
        runTest {
            val newValue = 123456789L
            whenever(getIncomingSharesChildrenNodeUseCase(newValue)).thenReturn(
                listOf<ShareFolderNode>(mock(), mock())
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)
            val update = mapOf<Node, List<NodeChanges>>(
                mock<Node>() to emptyList(),
                mock<Node>() to emptyList()
            )
            monitorNodeUpdatesFakeFlow.emit(NodeUpdate(update))
            underTest.setCurrentHandle(newValue)
            assertThat(underTest.state.value.nodesList.size).isEqualTo(2)
        }

    @Test
    fun `test that no nodes are returned when setting the file browser handle and the file browser node is null`() =
        runTest {
            val newValue = 123456789L
            whenever(getIncomingSharesChildrenNodeUseCase.invoke(newValue)).thenReturn(emptyList())
            underTest.setCurrentHandle(newValue)
            assertThat(underTest.state.value.nodesList.size).isEqualTo(0)
            verify(getIncomingSharesChildrenNodeUseCase).invoke(newValue)
        }

    @Test
    fun `test that the nodes are not retrieved when a back navigation is performed and the parent handle is null`() =
        runTest {
            val newValue = 123456789L
            underTest.performBackNavigation()
            verify(getIncomingSharesChildrenNodeUseCase, times(0)).invoke(newValue)
        }

    @Test
    fun `test that get file browser node children use case is invoked when a back navigation is performed and the parent handle exists`() =
        runTest {
            val newValue = 123456789L
            // to update handles fileBrowserHandle
            whenever(getIncomingSharesChildrenNodeUseCase.invoke(newValue)).thenReturn(
                listOf<ShareFolderNode>(mock(), mock())
            )
            underTest.setCurrentHandle(newValue)
            underTest.performBackNavigation()
            verify(getIncomingSharesChildrenNodeUseCase).invoke(newValue)
        }

    @Test
    fun `test that the selected node handle count is incremented when a node is long clicked`() =
        runTest {
            val nodesListItem1 = mock<ShareFolderNode>()
            val nodesListItem2 = mock<ShareFileNode>()
            whenever(nodesListItem1.id.longValue).thenReturn(1L)
            whenever(nodesListItem2.id.longValue).thenReturn(2L)
            whenever(getIncomingSharesChildrenNodeUseCase(underTest.state.value.currentHandle)).thenReturn(
                listOf(nodesListItem1, nodesListItem2)
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)
            underTest.refreshNodes()
            underTest.onLongItemClicked(
                NodeUIItem(
                    nodesListItem1,
                    isSelected = false,
                    isInvisible = false
                )
            )
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.totalSelectedFolderNodes).isEqualTo(1)
                assertThat(state.totalSelectedFileNodes).isEqualTo(0)
                assertThat(state.selectedNodeHandles.size).isEqualTo(1)
            }
        }

    @Test
    fun `test that the selected node handle count is decremented when one of the selected nodes is clicked`() =
        runTest {
            val nodesListItem1 = mock<ShareFolderNode>()
            val nodesListItem2 = mock<ShareFileNode>()
            whenever(nodesListItem1.id.longValue).thenReturn(1L)
            whenever(nodesListItem2.id.longValue).thenReturn(2L)
            whenever(getIncomingSharesChildrenNodeUseCase(underTest.state.value.currentHandle)).thenReturn(
                listOf(nodesListItem1, nodesListItem2)
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)

            underTest.refreshNodes()
            underTest.onLongItemClicked(
                NodeUIItem(
                    nodesListItem1,
                    isSelected = false,
                    isInvisible = false
                )
            )
            underTest.onItemClicked(
                NodeUIItem(
                    nodesListItem1,
                    isSelected = true,
                    isInvisible = false
                )
            )
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.totalSelectedFolderNodes).isEqualTo(0)
                assertThat(state.totalSelectedFileNodes).isEqualTo(0)
                assertThat(state.selectedNodeHandles.size).isEqualTo(0)
            }
        }

    @Test
    fun `test that the selected node handle count is incremented when the selected node is clicked`() =
        runTest {
            val nodesListItem1 = mock<ShareFileNode>()
            val nodesListItem2 = mock<ShareFolderNode>()
            whenever(nodesListItem1.id.longValue).thenReturn(1L)
            whenever(nodesListItem2.id.longValue).thenReturn(2L)

            whenever(getIncomingSharesChildrenNodeUseCase(underTest.state.value.currentHandle)).thenReturn(
                listOf(nodesListItem1, nodesListItem2)
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)
            underTest.refreshNodes()
            underTest.onLongItemClicked(
                NodeUIItem(
                    nodesListItem1,
                    isSelected = false,
                    isInvisible = false
                )
            )
            underTest.onItemClicked(
                NodeUIItem(
                    nodesListItem2,
                    isSelected = false,
                    isInvisible = false
                )
            )
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.totalSelectedFolderNodes).isEqualTo(1)
                assertThat(state.totalSelectedFileNodes).isEqualTo(1)
                assertThat(state.selectedNodeHandles.size).isEqualTo(2)
            }
        }

    @Test
    fun `test that set view type is called when changing the view type`() =
        runTest {
            underTest.onChangeViewTypeClicked()
            verify(setViewType).invoke(ViewType.GRID)
        }

    @Test
    fun `test that the sizes of both selected node handles and the nodes are equal when selecting all nodes`() =
        runTest {
            whenever(getIncomingSharesChildrenNodeUseCase(underTest.state.value.currentHandle)).thenReturn(
                listOf<ShareFolderNode>(mock(), mock())
            )
            whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)
            underTest.refreshNodes()
            underTest.selectAllNodes()
            assertThat(underTest.state.value.nodesList.size)
                .isEqualTo(underTest.state.value.selectedNodeHandles.size)
        }

    @Test
    fun `test that contact not verified banner is false when current node is root`() =
        runTest {
            val nodeHandle = -1L
            whenever(getIncomingSharesChildrenNodeUseCase.invoke(nodeHandle)).thenReturn(
                listOf<ShareFolderNode>(
                    mock(),
                    mock()
                )
            )
            underTest.setCurrentHandle(nodeHandle)
            assertThat(underTest.state.value.showContactNotVerifiedBanner).isFalse()
        }


    @Test
    fun `test that contact not verified banner is false when credentials are verified`() =
        runTest {
            val nodeHandle = 123456789L
            val email = "user@mail.com"
            whenever(getIncomingSharesChildrenNodeUseCase.invoke(nodeHandle)).thenReturn(
                listOf<ShareFolderNode>(
                    mock(),
                    mock()
                )
            )
            whenever(getIncomingShareParentUserEmailUseCase.invoke(NodeId(nodeHandle))).thenReturn(
                email
            )
            whenever(areCredentialsVerifiedUseCase.invoke(email)).thenReturn(true)
            underTest.setCurrentHandle(nodeHandle)
            assertThat(underTest.state.value.showContactNotVerifiedBanner).isFalse()
        }

    @Test
    fun `test that contact not verified banner is true when conditions are fulfilled`() =
        runTest {
            val nodeHandle = 123456789L
            val email = "user@mail.com"
            whenever(getIncomingSharesChildrenNodeUseCase.invoke(nodeHandle)).thenReturn(
                listOf<ShareFolderNode>(
                    mock(),
                    mock()
                )
            )
            whenever(getIncomingShareParentUserEmailUseCase.invoke(NodeId(nodeHandle))).thenReturn(
                email
            )
            whenever(areCredentialsVerifiedUseCase.invoke(email)).thenReturn(false)
            underTest.setCurrentHandle(nodeHandle)
            assertThat(underTest.state.value.showContactNotVerifiedBanner).isTrue()
        }

    @Test
    fun `test that the selected node handles is empty when clearing all nodes`() = runTest {
        underTest.clearAllNodes()
        underTest.state.test {
            val state = awaitItem()
            assertThat(state.selectedNodeHandles).isEmpty()
        }
    }

    @Test
    fun `test that handle option click mapper is invoked when selecting an option item`() =
        runTest {
            val menuItem: MenuItem = mock()
            whenever(handleOptionClickMapper.invoke(menuItem, emptyList())).thenReturn(
                OptionsItemInfo(
                    optionClickedType = OptionItems.MOVE_CLICKED,
                    selectedNode = emptyList(),
                    selectedMegaNode = emptyList()
                )
            )
            underTest.onOptionItemClicked(item = menuItem)
            verify(handleOptionClickMapper).invoke(menuItem, emptyList())
        }

    @Test
    fun `test that is pending refresh is true when a node refresh event is emitted`() = runTest {
        val flow = MutableSharedFlow<Unit>()
        whenever(monitorRefreshSessionUseCase()).thenReturn(flow)
        initViewModel()
        advanceUntilIdle()
        underTest.state.test {
            assertThat(awaitItem().isPendingRefresh).isFalse()
            flow.emit(Unit)
            assertThat(awaitItem().isPendingRefresh).isTrue()
        }
    }

    @Test
    fun `test that download event is updated when on download option click is invoked and both download option and feature flag are true`() =
        runTest {
            onDownloadOptionClick()
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.downloadEvent).isInstanceOf(StateEventWithContentTriggered::class.java)
                assertThat((state.downloadEvent as StateEventWithContentTriggered).content)
                    .isInstanceOf(TransferTriggerEvent.StartDownloadNode::class.java)
            }
        }

    @Test
    fun `test that download event is cleared when the download event is consumed`() =
        runTest {
            //first set to triggered
            onDownloadOptionClick()
            //now we can test consume clears the state
            underTest.consumeDownloadEvent()
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.downloadEvent).isInstanceOf(StateEventWithContentConsumed::class.java)
            }
        }

    private suspend fun onDownloadOptionClick() {
        val menuItem = mock<MenuItem>()
        val optionsItemInfo =
            OptionsItemInfo(OptionItems.DOWNLOAD_CLICKED, emptyList(), emptyList())
        whenever(handleOptionClickMapper(eq(menuItem), any())).thenReturn(optionsItemInfo)
        underTest.onOptionItemClicked(menuItem)
    }

    @Test
    fun `test that download event is updated when on available offline option click is invoked`() =
        runTest {
            val triggered = TransferTriggerEvent.StartDownloadForOffline(
                node = mock(),
                withStartMessage = false
            )
            underTest.onDownloadFileTriggered(triggered)
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.downloadEvent).isInstanceOf(StateEventWithContentTriggered::class.java)
                assertThat((state.downloadEvent as StateEventWithContentTriggered).content)
                    .isInstanceOf(TransferTriggerEvent.StartDownloadForOffline::class.java)
                assertThat((state.downloadEvent.content as TransferTriggerEvent.StartDownloadForOffline).withStartMessage)
                    .isFalse()
            }
        }

    @Test
    fun `test that download event is updated when on download option click is invoked`() =
        runTest {
            val triggered = TransferTriggerEvent.StartDownloadNode(
                nodes = listOf(mock()),
                withStartMessage = false
            )
            underTest.onDownloadFileTriggered(triggered)
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.downloadEvent).isInstanceOf(StateEventWithContentTriggered::class.java)
                assertThat((state.downloadEvent as StateEventWithContentTriggered).content)
                    .isInstanceOf(TransferTriggerEvent.StartDownloadNode::class.java)
                assertThat((state.downloadEvent.content as TransferTriggerEvent.StartDownloadNode).withStartMessage)
                    .isFalse()
            }
        }

    @Test
    fun `test that download event is updated when on download for preview option click is invoked`() =
        runTest {
            val triggered = TransferTriggerEvent.StartDownloadForPreview(node = mock(), isOpenWith = false)
            underTest.onDownloadFileTriggered(triggered)
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.downloadEvent).isInstanceOf(StateEventWithContentTriggered::class.java)
                assertThat((state.downloadEvent as StateEventWithContentTriggered).content)
                    .isInstanceOf(TransferTriggerEvent.StartDownloadForPreview::class.java)
            }
        }

    @Test
    fun `test that leave share dialog is shown when setShowLeaveShareConfirmationDialog is called`() =
        runTest {
            val handles = listOf(1L, 2L, 3L)
            underTest.setShowLeaveShareConfirmationDialog(handles)
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.showConfirmLeaveShareEvent).isInstanceOf(
                    StateEventWithContentTriggered::class.java
                )
                assertThat((state.showConfirmLeaveShareEvent as StateEventWithContentTriggered<List<Long>>).content).isEqualTo(
                    handles
                )
            }
        }

    @Test
    fun `test that leave share dialog is consumed when consumeShowLeaveShareConfirmationDialog is called`() =
        runTest {
            underTest.consumeShowLeaveShareConfirmationDialog()
            underTest.state.test {
                val state = awaitItem()
                assertThat(state.showConfirmLeaveShareEvent).isInstanceOf(
                    StateEventWithContentConsumed::class.java
                )
            }
        }

    private suspend fun stubCommon() {
        whenever(monitorNodeUpdatesUseCase()).thenReturn(monitorNodeUpdatesFakeFlow)
        whenever(monitorViewType()).thenReturn(emptyFlow())
        whenever(getIncomingSharesChildrenNodeUseCase(any())).thenReturn(emptyList())
        whenever(getParentNodeUseCase(NodeId(any()))).thenReturn(null)
        whenever(getCloudSortOrder()).thenReturn(SortOrder.ORDER_NONE)
        whenever(getOthersSortOrder()).thenReturn(SortOrder.ORDER_NONE)
        whenever(monitorRefreshSessionUseCase()).thenReturn(emptyFlow())
        whenever(fileDurationMapper(any())).thenReturn(1.seconds)
        whenever(monitorOfflineNodeUpdatesUseCase()).thenReturn(emptyFlow())
        whenever(monitorContactUpdatesUseCase()).thenReturn(emptyFlow())
        whenever(monitorConnectivityUseCase()).thenReturn(emptyFlow())
        whenever(getContactVerificationWarningUseCase()).thenReturn(true)
        whenever(areCredentialsVerifiedUseCase(any())).thenReturn(true)
        whenever(getIncomingShareParentUserEmailUseCase(NodeId(any()))).thenReturn("")
        whenever(getNodeByIdUseCase(NodeId(any()))).thenReturn(mock())
    }

    @AfterEach
    fun resetMocks() {
        reset(
            monitorNodeUpdatesUseCase,
            getParentNodeUseCase,
            getIncomingSharesChildrenNodeUseCase,
            getCloudSortOrder,
            getOthersSortOrder,
            monitorContactUpdatesUseCase,
            handleOptionClickMapper,
            monitorViewType,
            setViewType,
            monitorRefreshSessionUseCase,
            fileDurationMapper,
            monitorOfflineNodeUpdatesUseCase,
            monitorConnectivityUseCase,
            durationInSecondsTextMapper,
            getContactVerificationWarningUseCase,
            areCredentialsVerifiedUseCase,
            getIncomingShareParentUserEmailUseCase,
            getNodeByIdUseCase
        )
    }
}
