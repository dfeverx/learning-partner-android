package app.dfeverx.ninaiva.ui.screens.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.ui.Screens
import app.dfeverx.ninaiva.ui.components.ModernGrid
import app.dfeverx.ninaiva.ui.components.ProgressCard
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Statistics(navController: NavController) {
    val statisticsViewModel = hiltViewModel<StatisticsViewModel>()
    val statisticUiState by statisticsViewModel.uiState.collectAsState()
    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())


    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                modifier = Modifier/*.borderBottom()*/,
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
                actions = {

                },
                scrollBehavior = scrollBehavior
            )

        }, content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    PlayResult(statisticUiState, isGoToHomeScreen = {
                        when (it) {
                            true -> {
//                                navController.nav(Screens.Home.route)
                            }

                            false -> {
                                navController.popBackStack()
                                navController.navigate(Screens.Play.route + statisticsViewModel.playRetry())
                            }
                        }
                    })
                }
            }


        })
}


@Composable
fun PlayResult(statisticUiState: StatisticsUiState, isGoToHomeScreen: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

//icon of level
        val composition by rememberLottieComposition(
            LottieCompositionSpec
                .RawRes(if (statisticUiState.isSuccessfulAttempt) R.raw.cat_happy else R.raw.cat_crying)
        )
        val logoAnimationState = animateLottieCompositionAsState(
            composition = composition, iterations = 3, restartOnPlay = false
        )

        Text(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(horizontal = 16.dp),
            text = if (statisticUiState.isSuccessfulAttempt) "Congratulations" else "Oops!, Try again",
            style = MaterialTheme.typography.displaySmall
        )
        val descriptionText = when (statisticUiState.isSuccessfulAttempt) {
            true -> "Level ${statisticUiState.stage} Completed"
            false -> "Learn from the mistake and try again, Level ${statisticUiState.stage}"

        }
        Text(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(horizontal = 16.dp),
            text = descriptionText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        ModernGrid(modifier = Modifier.padding(bottom = 16.dp), content = {
            /*Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Outlined.Lock,
                contentDescription = ""
            )*/
            LottieAnimation(modifier = Modifier
                .fillMaxSize(),
                composition = composition,
                progress = { logoAnimationState.progress })
        }) {

        }

        ProgressCard(
            score = statisticUiState.score,
            levelStage = statisticUiState.stage,
            accuracy = statisticUiState.accuracy,
            levelProgress = statisticUiState.levelOverAllProgress
        ) {

        }

        Button(
            modifier = Modifier.padding(16.dp),
            onClick = { isGoToHomeScreen(statisticUiState.isSuccessfulAttempt) }) {
            if (statisticUiState.isSuccessfulAttempt) {
                Icon(imageVector = Icons.Outlined.Home, contentDescription = "")
                Text(modifier = Modifier.padding(start = 4.dp), text = "Go to home")
            } else {
                Icon(imageVector = Icons.Outlined.Replay, contentDescription = "")
                Text(modifier = Modifier.padding(start = 4.dp), text = "Retry now")
            }
        }
    }
}
