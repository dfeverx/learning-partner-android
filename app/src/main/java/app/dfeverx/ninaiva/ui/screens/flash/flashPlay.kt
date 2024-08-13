package app.dfeverx.ninaiva.ui.screens.flash

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.dfeverx.ninaiva.utils.borderBottom


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FlashPlay(navController: NavController) {
    val flashViewModel = hiltViewModel<FlashViewModel>()
    val flashCards = flashViewModel.flashCards.collectAsState()
    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(modifier = Modifier, topBar = {
        TopAppBar(
            modifier = Modifier.borderBottom(),
            navigationIcon = {
                Icon(
                    Icons.Outlined.Close,
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
    }, bottomBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = "")
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = "Double tap the card to reveal"
            )
        }
    }, content = { paddingValues ->

        val pagerState = rememberPagerState(pageCount = {
            flashCards.value.size
        }, initialPage =flashViewModel.currentCardIndex)

        VerticalPager(state = pagerState, modifier = Modifier.padding(paddingValues)) { page ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FlippableCard(/*rotated = isRotated, onRotate = { isRotated = !isRotated }*/
                    flashCards.value.get(page)
                )
            }
        }
    })
}