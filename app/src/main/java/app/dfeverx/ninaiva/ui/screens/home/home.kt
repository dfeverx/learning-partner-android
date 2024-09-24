package app.dfeverx.ninaiva.ui.screens.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.window.core.layout.WindowWidthSizeClass.Companion.COMPACT
import androidx.window.core.layout.WindowWidthSizeClass.Companion.MEDIUM
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.datastore.StreakInfo
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.ui.Screens
import app.dfeverx.ninaiva.ui.components.AsyncThumbnail
import app.dfeverx.ninaiva.ui.components.FilledInLineIconTextCard
import app.dfeverx.ninaiva.ui.components.NoInternetConnectionCard
import app.dfeverx.ninaiva.ui.components.StreakCount
import app.dfeverx.ninaiva.ui.main.MainViewModel
import app.dfeverx.ninaiva.ui.screens.home.update.InAppUpdateCard
import app.dfeverx.ninaiva.utils.activityViewModel
import app.dfeverx.ninaiva.utils.borderBottom
import app.dfeverx.ninaiva.utils.openNotificationSettings
import app.dfeverx.ninaiva.utils.relativeTime
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import app.dfeverx.ninaiva.models.local.NOTE_PROCESSING_FUN_CALLING
import app.dfeverx.ninaiva.models.local.NOTE_PROCESSING_UPLOADING_PDF
import app.dfeverx.ninaiva.ui.main.MainActivity.Companion.acknowledged
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun Home(navController: NavHostController) {

    val mainViewModel = activityViewModel<MainViewModel>()
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val hasQuota = mainViewModel.quotaExceeds.collectAsState(initial = true)
    val hasInternet = mainViewModel.hasInternetConnection.collectAsState()
    val inAppUpdateState = mainViewModel.inAppUpdateState.collectAsState()
    val noteCategories by homeViewModel.subjectAndGrouping.collectAsState()
    val selectedCategory by homeViewModel.selectedCategory.collectAsState()
    val isPurchaseAcknowledged by mainViewModel.purchaseAcknowledgement.collectAsState()
    val studyNotes = homeViewModel.studyNotes.collectAsState()
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showProfileBs by rememberSaveable { mutableStateOf(false) }
    var showScanLimitBS by rememberSaveable { mutableStateOf(false) }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val activity = LocalContext.current as Activity
    val firebaseUser = Firebase.auth

    var showStreakBS by rememberSaveable { mutableStateOf(false) }
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        null
    }

    val isAlarmPermissionVisible = rememberSaveable {
        mutableStateOf(true)
    }

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val documentScanningResult =
                GmsDocumentScanningResult.fromActivityResultIntent(
                    result.data
                )

            documentScanningResult?.pdf?.let { pdf ->

                Log.d("TAG", "DocumentScanner: ${pdf.uri}")
//                homeViewModel.createStudyNoteFromPdf(pdf.uri)
                homeViewModel.initCreateNote(pdf.uri)
            }
        }
    }
    val isPro = mainViewModel.isPro.collectAsState()
    val snackbarMessage by homeViewModel.snackbarMessage.collectAsState()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            coroutineScope.launch {
                snackBarHostState.showSnackbar(message)
            }
        }
    }
    fun launchScanner() {
        homeViewModel
            .getScanner(isPro.value)
            .getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(
                    IntentSenderRequest.Builder(intentSender).build()
                )
            }.addOnFailureListener {
                Log.d("TAG", "DocumentScanner: Error :${it.message}")
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(
                        "Failed to launch document scanner!"
                    )
                }
            }
    }


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        bottomBar = {
            AnimatedVisibility(
                !hasInternet.value,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                NoInternetConnectionCard()
            }
        },
        topBar = {
            TopAppBar(modifier = Modifier, title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        modifier = Modifier.size(52.dp),
                        model = R.drawable.ic_launcher_foreground,
                        contentDescription = "Ninaiva logo icon",

                        )
                    AsyncImage(
                        modifier = Modifier.height(16.dp),
                        model = R.drawable.ninaiva_typography_logo,
                        contentDescription = "Ninaiva logo typography",

                        )

                }
            }, scrollBehavior = scrollBehavior, actions = {
                AnimatedVisibility(!showProfileBs && !showStreakBS) {
                    StreakCount(
                        true, creditRemain = mainViewModel.streakInfoFlow.collectAsState(
                            initial = StreakInfo(0, 0)
                        ).value.count
                    ) {
                        showStreakBS = true
                    }
                }


                IconButton(onClick = {

                }, modifier = Modifier.padding(end = 8.dp)) {

                    AsyncImage(model = if (firebaseUser.currentUser?.isAnonymous == true) "https://api.dicebear.com/8.x/avataaars-neutral/png?seed=Precious" else firebaseUser.currentUser?.photoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                showProfileBs = true
                            })

                }
            })
        },
        floatingActionButton = {
            if (!(studyNotes.value.isEmpty() && selectedCategory == "all")) {
                FloatingScannerButton(
                    expanded = lazyStaggeredGridState.firstVisibleItemIndex < 2,
                    onClick = {
                        if (mainViewModel.directScanEligible()) {
                            launchScanner()
                        } else {
                            showScanLimitBS = true
                        }
                    }
                )

            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxHeight()
                    .fillMaxWidth()
            ) {
                if (studyNotes.value.isNotEmpty() || selectedCategory != "all") {
                    LazyRow(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .then(
                                if (lazyStaggeredGridState.firstVisibleItemScrollOffset > 0 || studyNotes.value.isEmpty()) Modifier.borderBottom() else Modifier.borderBottom(
                                    color = Color.Transparent
                                )
                            )
                            .padding(vertical = 4.dp)

                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(noteCategories) {

                            AssistChip(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .then(if (it.isPlaceholder) Modifier.shimmer(shimmerInstance) else Modifier),
                                colors = ChipColors(
                                    containerColor = if (it.value == selectedCategory) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    disabledLeadingIconContentColor = Color.Transparent,
                                    labelColor = if (it.value == selectedCategory) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    trailingIconContentColor = Color.Transparent,
                                    leadingIconContentColor = if (it.value == selectedCategory) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    disabledTrailingIconContentColor = Color.Transparent
                                ),
                                onClick = {
                                    /*coroutineScope.launch {
                                        snackBarHostState.showSnackbar(
                                            "Snackbar #"
                                        )
                                    }
                                    return@AssistChip*/
                                    homeViewModel.handleCategorySelection(it.value)
                                },
                                label = {
                                    if (it.isPlaceholder) Box(
                                        modifier = Modifier
                                            .height(16.dp)
                                            .width(72.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) else Text(
                                        it.label
                                    )
                                },
                                leadingIcon = {
                                    if (it.isPlaceholder) Box(
                                        modifier = Modifier
                                            .size(AssistChipDefaults.IconSize)
                                            .clip(
                                                CircleShape
                                            )

                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) else Icon(
                                        it.icon,
                                        contentDescription = "Localized description",
                                        Modifier
                                            .size(AssistChipDefaults.IconSize)


                                    )

                                },
                                shape = MaterialTheme.shapes.extraLarge
                            )
                        }
                    }
                }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(
                        if (studyNotes.value.isEmpty()) 1
                        else if (windowSizeClass.windowWidthSizeClass == COMPACT) {
                            val k = if (studyNotes.value.size < 2) studyNotes.value.size else 2
                            k
                        } else if (windowSizeClass.windowWidthSizeClass == MEDIUM) {
                            val k = if (studyNotes.value.size < 3) studyNotes.value.size else 3
                            k
                        } else {
                            val k = if (studyNotes.value.size < 4) studyNotes.value.size else 4
                            k
                        }
                    ),
                    state = lazyStaggeredGridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp, bottom = 32.dp, start = 16.dp, end = 16.dp
                    ),
                    verticalItemSpacing = 16.dp,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    //                show inapp update only if appupdate info available
                    if (!hasQuota.value) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            UtilityCard(
                                title = if (mainViewModel.auth.currentUser?.isAnonymous == true) "Quota exceeds!" else if (isPro.value) "Monthly quota exceeds!" else "Monthly quota exceeds!",
                                description =
                                if (mainViewModel.auth.currentUser?.isAnonymous == true) "Authenticate your account and get up to 5Notes/month for free" else if (isPro.value) "Revise the existing notes, wait for the next month to create new !" else "Upgrade to pro plans to scan up to 50 Notes/month or wait for the next month  ",
                                icon = Icons.Outlined.Warning,
                                askText = null
                            ) {

                            }
                        }
                    }
                    if (inAppUpdateState.value.appUpdateInfo != null && inAppUpdateState.value.isVisible) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            InAppUpdateCard(state = inAppUpdateState.value)
                        }
                    }
//alarm permission
                    if (!mainViewModel.hasAlarmPermission() && isAlarmPermissionVisible.value) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            UtilityCard(
                                title = "Alarm permission",
                                description = "Inorder to set remainder for the study note , alarm permission needed",
                                icon = Icons.Outlined.Alarm
                            ) {
                                if (mainViewModel.hasAlarmPermission()) {
                                    isAlarmPermissionVisible.value = false
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    startActivity(
                                        context,
                                        Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM), null
                                    )
                                }
                            }
                        }
                    }
//                    purchase ack
                    if (isPurchaseAcknowledged != null) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            UtilityCard(
                                title = "Subscription successful",
                                description = "You are successfully subscribed to Ninaiva Pro plan",
                                icon = Icons.Outlined.WorkspacePremium,
                                askText = "OK",
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                isPurchaseAcknowledged?.acknowledged()
                                mainViewModel.updatePurchaseAcknowledgement(null)
                            }
                        }
                    }

//notification permission
                    if (notificationPermission?.status?.isGranted == false) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            UtilityCard {
                                if (notificationPermission.status.shouldShowRationale) {
                                    context.openNotificationSettings()
                                } else {
                                    notificationPermission.launchPermissionRequest()
                                }
                            }
                        }
                    }



                    if (studyNotes.value.isEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            EmptyStudyNote(
                                modifier = Modifier.fillMaxHeight(),
                                category = selectedCategory,
                                action = {
                                    if (selectedCategory == "all") {
                                        FloatingScannerButton(
                                            expanded = lazyStaggeredGridState.firstVisibleItemIndex < 2,
                                            onClick = {
                                                if (mainViewModel.directScanEligible()) {
                                                    launchScanner()
                                                } else {
                                                    showScanLimitBS = true
                                                }
                                            }
                                        )
                                    }

                                })
                        }
                    }

                    items(studyNotes.value) { studyNote ->
                        if (studyNote.isPlaceholder) {
                            StudyNoteOverviewPlaceholderItem(studyNote = studyNote,
                                homeViewModel = homeViewModel,
                                onClickNote = {},
                                {})
                        } else {

                            StudyNoteOverviewItem(studyNote = studyNote,
                                hasQuota = hasQuota.value,
                                onClickNote = {
                                    if (notificationPermission?.status?.isGranted == false) {
                                        notificationPermission.launchPermissionRequest()
                                        Toast.makeText(
                                            activity,
                                            "Enable notification permission before opening notes",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                        return@StudyNoteOverviewItem
                                    }
                                    navController.navigate(
                                        Screens.NoteDetails.route + "/" + it.id
                                    )
                                },
                                onClickTag = {
                                    homeViewModel.handleCategorySelection(it)
                                },
                                openDocument = {
                                    if (notificationPermission?.status?.isGranted == false) {
                                        notificationPermission.launchPermissionRequest()
                                        Toast.makeText(
                                            activity,
                                            "Enable notification permission before for scanning notes",
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                        return@StudyNoteOverviewItem
                                    }
                                    navController.navigate(
                                        Screens.DocViewer.route + "/" + it.id
                                    )
                                }, retry = {
                                    homeViewModel.retryCreateNote(it)
                                    coroutineScope.launch {
                                        lazyStaggeredGridState.scrollToItem(0)
                                    }
                                })
                        }
                    }

                }
            }

            /*
              */

        })
    if (showProfileBs) {
        ModalBottomSheet(
            onDismissRequest = {
                showProfileBs = false
            },
            sheetState = bottomSheetState,
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom)
        ) {

            UserProfileBottomSheetContent(userProfile = firebaseUser.currentUser,
                navController,
                isUserProfile = true,
                creditRemain = mainViewModel.streakInfoFlow.collectAsState(
                    initial = StreakInfo(
                        0,
                        0
                    )
                ).value.count,
                viewStreak = {
                    showProfileBs = false
                    showStreakBS = true
                },
                actionDone = {
                    showProfileBs = false
                })

        }
    }

    if (showStreakBS) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = {
                showStreakBS = false
            },
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom)
        ) {
            StreakBottomSheetContent(
                streakInfo = mainViewModel.streakInfoFlow.collectAsState(
                    initial = StreakInfo(0, 0)
                ),
                schedules = homeViewModel.repetitionSchedules.collectAsState().value
            )
        }
    }
    if (showScanLimitBS) {
        ModalBottomSheet(
            onDismissRequest = {
                showScanLimitBS = false
            },
            sheetState = bottomSheetState,
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom)
        ) {

            LimitScanBottomSheetContent(
                isPro = isPro.value,
                isAnonymous = homeViewModel.isAnonymousUser(),
                noteCount = studyNotes.value.size,
                isEligibleForNoteCreation = mainViewModel.isEligibleForNoteCreation(),
                onError = {
                    Log.d("TAG", "Home: error on login $it")
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(
                            it
                        )
                    }
                },
                launchScanner = {
                    showScanLimitBS = false
                    launchScanner()
                }, navigateTo = {
                    showScanLimitBS = false
                    navController.navigate(it)
                })
        }
    }
}


@Composable
fun StudyNoteOverviewItem(
    studyNote: StudyNote,

    hasQuota: Boolean,
    onClickNote: (StudyNote) -> Unit,
    onClickTag: (String) -> Unit,
    openDocument: (StudyNote) -> Unit,
    retry: (StudyNote) -> Unit
) {

    OutlinedCard(modifier = Modifier, onClick = {
        if (studyNote.status > 2) {
            onClickNote(studyNote)
        }
    }) {
        val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
        if (studyNote.status != -1) {
            AsyncThumbnail(
                modifier = Modifier
                    .aspectRatio(16f / 9f)
                    .fillMaxWidth()
                    .then(
                        if (studyNote.isPlaceholder) Modifier.shimmer(
                            shimmerInstance
                        ) else Modifier
                    ), url = studyNote.thumbnail, contentDescription = ""
            )
        }

        if (studyNote.isProcessing) {
            ProcessingNote(studyNote = studyNote)
        } else if (studyNote.status in 0..2) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                contentColor = if (hasQuota) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer,
                color = if (hasQuota) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        Text(
                            modifier = Modifier,
                            text = if (hasQuota) "Failed" else "Failed, you can retry this after you have a valid quota!",
                            color = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (hasQuota) {
                            Button(onClick = {
                                retry(studyNote)
                            }) {
                                Text(text = "Retry")
                            }
                        }


                    }
                }
            }
        } else if (studyNote.status == -1) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.error)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Error,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.padding(4.dp)
                    )
                    Text(
                        text = "Suspended",
                        color = MaterialTheme.colorScheme.errorContainer,
                        style = MaterialTheme.typography.titleMedium,

                        )
                }
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = "The given note is not a valid study note,uploading invalid note leads to account termination",
                    color = MaterialTheme.colorScheme.error
                )
            }

        } else {
            if (studyNote.nextLevelIn.toInt() != 0) {
                FilledInLineIconTextCard(
                    icon = Icons.Outlined.CalendarToday,
                    description = studyNote.nextLevelIn.relativeTime()
                )
                /*       Row(
                           modifier = Modifier
                               .fillMaxWidth()
                               .padding(8.dp)
                               .clip(MaterialTheme.shapes.large)
                               .background(MaterialTheme.colorScheme.surfaceVariant)
                               .padding(8.dp),
                           verticalAlignment = Alignment.CenterVertically
                       ) {
                           Icon(
                               imageVector = Icons.Outlined.Notifications,
                               contentDescription = ""
                           )
                           Text(
                               text = homeViewModel.formatTime(studyNote.nextLevelIn),
                               modifier = Modifier
                                   .padding(8.dp), style = MaterialTheme.typography.labelMedium

                           )
                       }*/
            }
            Text(
                modifier = Modifier.padding(8.dp),
                text = studyNote.title,
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                AssistChip(modifier = Modifier,
                    onClick = { onClickTag(studyNote.subject) },
                    label = {
                        Text(
                            text = studyNote.subject,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    })


            }

            Text(
                text = studyNote.summary, modifier = Modifier.padding(8.dp)
            )

        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { openDocument(studyNote) }
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.RemoveRedEye,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp),
                contentDescription = "",
            )
            Text(text = "Document")
        }

    }
}

@Composable
fun ProcessingNote(modifier: Modifier = Modifier, studyNote: StudyNote) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val color1 by infiniteTransition.animateColor(
        initialValue = Color.Red,
        targetValue = Color.Blue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = Color.Blue,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val brush = Brush.horizontalGradient(listOf(color1, color2))
    Column(modifier = modifier
        .fillMaxWidth()
        .animateContentSize()
        .drawBehind {
            drawRect(brush)
        }) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            val rotation = remember { Animatable(0f) }

            // Launch the animation to rotate infinitely
            LaunchedEffect(Unit) {
                rotation.animateTo(
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .rotate(rotation.value),
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "",
                tint = Color.White
            )
            Text(
                modifier = Modifier,
                text = "Processing wait..",
                color = Color.White
            )

        }

        androidx.compose.animation.AnimatedVisibility(studyNote.status == NOTE_PROCESSING_FUN_CALLING || studyNote.status == NOTE_PROCESSING_UPLOADING_PDF) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp),
                text = if (studyNote.status == NOTE_PROCESSING_UPLOADING_PDF) "1/3 Uploading" else if (studyNote.status == NOTE_PROCESSING_FUN_CALLING) "2/3 Key points" else "...",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }


    }
}


@Composable
fun StudyNoteOverviewPlaceholderItem(
    studyNote: StudyNote,
    homeViewModel: HomeViewModel,
    onClickNote: (StudyNote) -> Unit,
    onClickTag: (String) -> Unit,
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    OutlinedCard(
        modifier = Modifier.shimmer(
            shimmerInstance
        ) /*.padding(8.dp)*/, onClick = {
            if (!studyNote.isProcessing) {
                onClickNote(studyNote)
            }
        }, border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncThumbnail(
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .fillMaxWidth(),
            url = studyNote.thumbnail,
            contentDescription = ""
        )

        Row(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {

            Box(
                modifier = Modifier

                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(MaterialTheme.shapes.small)
                    .height(24.dp)
                    .width(72.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )


        }
//        title
        repeat(Random.nextInt(2, 4)) {
            Box(
                modifier = Modifier

                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(CircleShape)
                    .height(14.dp)
                    .fillMaxWidth(
                        Random
                            .nextInt(7, 10)
                            .div(10f)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)

            )
        }
        Spacer(modifier = Modifier.padding(top = 16.dp))
//        paragraph
        repeat(Random.nextInt(10, 40)) {
            Box(
                modifier = Modifier

                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(CircleShape)
                    .height(10.dp)
                    .fillMaxWidth(
                        Random
                            .nextInt(7, 10)
                            .div(10f)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)

            )
        }



        Divider(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(CircleShape)
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(CircleShape)
                    .height(12.dp)
                    .width(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }

}


@Composable
fun EmptyStudyNote(
    modifier: Modifier,
    category: String,
    action: @Composable () -> Unit
) {
    val msg = when (category) {
        "all" -> Pair(
            "Add your first note",
            "Scan your first note and start smart way to learn"
        )

        "starred" -> Pair(
            "You haven't starred",
            "Star the note that import to you that will appear here"
        )

        "learning" -> Pair(
            "No active learning notes",
            "Study notes that learning currently will appear here"
        )

        "archive" -> Pair(
            "You haven't completed one",
            "Study notes that you learned will appear here"
        )

        "trash" -> Pair(
            "Trash empty!",
            "Study notes that deleted will appear here"
        )

        else -> Pair(
            "Empty",
            "There is no study notes avilable to show"
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 84.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            modifier = Modifier.size(172.dp),
            model = R.drawable.illu_team_brainstorming,
            contentDescription = "Translated description of what the image contains",

            )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = msg.first,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = msg.second,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        action()


    }

}


class TextIcon(
    val icon: ImageVector, val label: String, val value: String,
) {
    var isPlaceholder: Boolean = false
}



