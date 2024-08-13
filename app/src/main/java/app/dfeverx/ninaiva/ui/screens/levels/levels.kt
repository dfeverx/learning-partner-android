package app.dfeverx.ninaiva.ui.screens.levels

import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.dfeverx.ninaiva.ui.Screens
import app.dfeverx.ninaiva.utils.TimePeriod
import app.dfeverx.ninaiva.utils.relativeTime
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Levels(navController: NavController) {
    val context = LocalContext.current
    val levelViewModel = hiltViewModel<LevelViewModel>()
    val levels by levelViewModel.levels.collectAsState()
    val noteId = navController.currentBackStackEntry?.arguments?.getString("noteId")
    val lazyGridState = rememberLazyListState()
    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var showLevelNotReadyBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            /*confirmValueChange = {
                       Log.d("TAG", "Play: call i want to quit")
                       false
                   }*/
        )
    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        TopAppBar(
            navigationIcon = {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    "Go back to previous",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable {
                            navController.navigateUp()
                        }
                        .padding(8.dp),
                )
            },
            title = {

            },
            scrollBehavior = scrollBehavior
        )

    }, content = { paddingValues ->

        LazyColumn(
            state = lazyGridState,
            modifier = Modifier
                .padding(paddingValues)

        ) {
            if (false) {
                item {
                    RandomPlayCard {

                    }
                }
            }



            items(levels) {
                it?.let { it1 ->
                    if (it1.isRevision) {
                        RevisionCardItem {}
                    } else {

                        LevelItemGroup(modifier = Modifier, level = it1, onClick = {
                            Log.d("TAG", "Levels: $it ")
                            if (it.levelId == null || (it.nextPlay == TimePeriod.FUTURE && it.stage == it.currentStage)) {
                                showLevelNotReadyBottomSheet = true
                            } else {
//                            Alarm check permission
                                if (levelViewModel.hasAlarmPermission()) {
                                    navController.navigate(Screens.Play.route + "/${noteId}/${it.levelId}/${it.stage}/${it.isRevision}")
                                } else {
                                    Toast.makeText(
                                        context,
                                        "To get accurate Notification update schedule alarm permission is mandatory",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        startActivity(
                                            context,
                                            Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM), null
                                        )
                                    }
                                }
                            }
                        })
                    }
                }
            }

            /*   items(2) {
                   RevisionCardItem {

                   }
               }*/


        }
    })
    if (showLevelNotReadyBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showLevelNotReadyBottomSheet = false
            },
            sheetState = bottomSheetState,
            windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Bottom)

        ) {

            LevelNotReadyBottomSheetContent(
                modifier = Modifier,
                handleOk = {
                    showLevelNotReadyBottomSheet = false
                })

        }
    }
}


@Composable
fun LevelItemGroup(modifier: Modifier, level: LevelUI, onClick: (LevelUI) -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(level) }
            .padding(horizontal = 16.dp)
    ) {


        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Box(modifier = Modifier, contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = "",
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(if (level.isPlayable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 4.dp,
                            color = if (level.isPlayable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                )
                Text(text = level.icon, style = MaterialTheme.typography.displaySmall)
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)

            ) {
                Text(text = level.name, style = MaterialTheme.typography.titleLarge)
                if (level.description.isNotEmpty() && level.nextPlayUnix.toInt() != 0) {
                    Text(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        text = level.nextPlayUnix.relativeTime(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            if (level.stage == level.currentStage) {
                when (level.nextPlay) {
                    TimePeriod.TODAY -> {
                        Button(onClick = { onClick(level) }) {
                            Text(text = "Play now")
                        }
                    }

                    TimePeriod.PAST -> {
                        Button(onClick = { onClick(level) }) {
                            Text(text = "Play now")
                        }
                    }

                    else -> {
                        Icon(
                            modifier = Modifier.padding(end = 16.dp),
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "", tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

            } else if (level.stage > level.currentStage) {
                Icon(
                    modifier = Modifier.padding(end = 16.dp),
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "", tint = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(78.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Divider(
                    color = if (level.currentStage > level.stage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                )
            }
            Column(
                modifier = Modifier
                    .heightIn(min = 64.dp)
                    .padding(bottom = 32.dp)
            ) {
//                todo: add level variations

            }

        }
    }
}


@Preview
@Composable
fun Preview() {

}