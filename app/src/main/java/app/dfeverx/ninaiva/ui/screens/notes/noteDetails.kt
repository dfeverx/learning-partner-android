package app.dfeverx.ninaiva.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.models.local.KeyArea
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.ui.Screens
import app.dfeverx.ninaiva.ui.components.AsyncThumbnail
import app.dfeverx.ninaiva.ui.components.FilledInLineIconTextCard
import app.dfeverx.ninaiva.ui.components.FlashCardOverview
import app.dfeverx.ninaiva.ui.components.ModernCard
import app.dfeverx.ninaiva.ui.components.NoInternetConnectionCard
import app.dfeverx.ninaiva.ui.components.ProgressCard
import app.dfeverx.ninaiva.ui.main.MainViewModel
import app.dfeverx.ninaiva.utils.activityViewModel
import app.dfeverx.ninaiva.utils.relativeTime
import dev.jeziellago.compose.markdowntext.MarkdownText


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetails(navController: NavHostController) {
    val mainViewModel = activityViewModel<MainViewModel>()
    val hasInternet = mainViewModel.hasInternetConnection.collectAsState()
    val noteViewModel = hiltViewModel<NoteViewModel>()
    val noteDetails by noteViewModel.studyNote.collectAsState()
    val flashCards by noteViewModel.flashCards.collectAsState()
    val noteId = navController.currentBackStackEntry?.arguments?.getString("noteId")
    val lazyGridState = rememberLazyListState()
    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var showMenu by remember { mutableStateOf(false) }

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
            actions = {
                IconButton(

                    onClick = {
                        navController.navigate(
                            Screens.DocViewer.route + "/" + noteDetails.id
                        )
                    }) {
                    Icon(
                        imageVector = Icons.Outlined.RemoveRedEye,
                        contentDescription = "View scanned document",

                        )
                }
                IconButton(

                    onClick = { noteViewModel.updatePinned(noteDetails) }) {
                    Icon(
                        imageVector = (if (noteDetails.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin),
                        contentDescription = "Pin the note",
                        tint = if (noteDetails.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { noteViewModel.updateStarred(noteDetails) }) {
                    Icon(
                        imageVector = if (noteDetails.isStarred) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = "Pin the note",
                        tint = if (noteDetails.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                /*
//                todo:delete note
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete"
                            )
                        },
                        onClick = { *//*TODO*//* },
                        text = { Text(text = "Move to trash") })
                }*/
            },
            title = {

            },
            scrollBehavior = scrollBehavior
        )

    }, bottomBar = {
        AnimatedVisibility(
            !hasInternet.value,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            NoInternetConnectionCard()
        }
    }, content = {

        LazyColumn(
            state = lazyGridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)

        ) {


            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    itemsIndexed(flashCards) { index, item ->
                        FlashCardOverview(info = item) {
                            navController.navigate(Screens.FlashPlay.route + "/" + noteId + "/" + index)
                        }
                    }

                }
            }
            if (noteDetails.currentStage > 1) {
                item {
                    FilledInLineIconTextCard(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        icon = Icons.Outlined.CalendarToday,
                        description = noteDetails.nextLevelIn.relativeTime()
                    )
                }
            }




            if (noteDetails.currentStage == 1) {
                item {
                    ModernCard {
                        navController.navigate(Screens.Levels.route + "/" + noteId)
                    }
                }
            }

            item {
                NoteHeaderDetails(noteDetails)
            }

            if (noteDetails.currentStage != 1) {
                item {
                    ProgressCard(
                        modifier = Modifier,
                        levelStage = noteDetails.currentStage - 1,
                        score = noteDetails.score,
                        accuracy = noteDetails.accuracy,
                        levelProgress = ((noteDetails.currentStage - 1)) / noteDetails.totalLevel.toFloat(),
                        isPlayButtonVisible = true,
                        continuePlay = { navController.navigate(Screens.Levels.route + "/" + noteId) })
                }
            }

            item {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 4.dp),
                    text = "Key points:",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            noteDetails.keyAreas.forEach {

                item {

                    KeyAreaItem(keyArea = it)
                }
            }


        }
    })
}

@Composable
fun NoteHeaderDetails(noteDetails: StudyNote) {
    Column {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            text = noteDetails.title,
            style = MaterialTheme.typography.displaySmall
        )
        AsyncThumbnail(url = noteDetails.thumbnail, contentDescription = "")
        /* AsyncImage(
             modifier = Modifier
                 .height(220.dp)
                 .padding(horizontal = 0.dp, vertical = 8.dp)
                 .background(MaterialTheme.colorScheme.surfaceVariant),
             model = noteDetails.thumbnail,
             contentDescription = "thumbnail image for ",
             contentScale = ContentScale.Crop
         )*/

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 4.dp),
            text = "Summary:",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 16.dp),
            text = noteDetails.summary,
            style = MaterialTheme.typography.bodyLarge
        )


    }
}


@Composable
fun KeyAreaItem(modifier: Modifier = Modifier, keyArea: KeyArea) {
    OutlinedCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .size(56.dp)
//                    .clip(CircleShape)
                    .paint(
                        painterResource(id = R.drawable.grid),
                        contentScale = ContentScale.FillBounds
                    )

                    .padding(12.dp),
                text = keyArea.emoji, style = MaterialTheme.typography.headlineSmall
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                text = keyArea.name,
                style = MaterialTheme.typography.headlineSmall
            )
        }
//         markdown support
        MarkdownText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), markdown = keyArea.info
        )

    }
}


@Preview

@Composable
fun PreviewAreaItem() {
    Surface {

        KeyAreaItem(
            keyArea = KeyArea(
                id = 1,
                emoji = "✅",
                name = "Laws of motions",
                info = "Newtons laws of motion state that ... "
            )
        )
    }
}


