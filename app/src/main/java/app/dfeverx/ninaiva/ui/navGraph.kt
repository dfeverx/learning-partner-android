package app.dfeverx.ninaiva.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.dfeverx.ninaiva.ui.screens.doc.DocViewer
import app.dfeverx.ninaiva.ui.screens.flash.FlashPlay
import app.dfeverx.ninaiva.ui.screens.statistics.Statistics
import app.dfeverx.ninaiva.ui.screens.home.Home
import app.dfeverx.ninaiva.ui.screens.levels.Levels
import app.dfeverx.ninaiva.ui.screens.notes.NoteDetails
import app.dfeverx.ninaiva.ui.screens.onboarding.Onboarding
import app.dfeverx.ninaiva.ui.screens.play.Play
import app.dfeverx.ninaiva.ui.screens.upgrade.Upgrade


@Composable
fun appNavHost(
    startDestination: String = Screens.Home.route,
    navController: NavHostController,
) {
    val uri = "ninaiva://app"
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(
            Screens.Onboarding.route,
            deepLinks = listOf(navDeepLink { uriPattern = "$uri/onboarding" })
        ) {
            Onboarding(navController)
        }
        composable(
            Screens.Home.route,
            deepLinks = listOf(navDeepLink { uriPattern = "$uri/home" })
        ) {
            Home(navController)
        }

        composable(
            route = Screens.NoteDetails.route + "/{noteId}",
            deepLinks = listOf(navDeepLink { uriPattern = "$uri/noteId={noteId}" }),
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = ""// Your parameter goes here
                },
            ),
        ) {
            NoteDetails(navController)
        }
        composable(
            route = Screens.DocViewer.route + "/{noteId}",
//            deepLinks = listOf(navDeepLink { uriPattern = "$uri/noteId={noteId}" }),
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            DocViewer(navController)
        }
        composable(
            route = Screens.Levels.route + "/{noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = "" // Your parameter goes here
                },
            ),
        ) {
            Levels(navController)
        }
        composable(
            route = Screens.Play.route + "/{noteId}/{levelId}/{stage}/{isRevision}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = "" // Your parameter goes here
                },
                navArgument("levelId") {
                    type = NavType.LongType
                    defaultValue = 0 // Your parameter goes here
                },
                navArgument("stage") {
                    type = NavType.IntType
                    defaultValue = 0 // Your parameter goes here
                },
                navArgument("isRevision") {
                    type = NavType.BoolType
                    defaultValue = false // Your parameter goes here
                },
            ),
        ) {
            Play(
                navController
            )
        }

        composable(
            route = Screens.FlashPlay.route + "/{noteId}/{currentCardIndex}",
            arguments = listOf(navArgument("noteId") {
                type = NavType.StringType
                defaultValue = ""
            },
                navArgument("currentCardIndex") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) {
            FlashPlay(navController)
        }
        composable(
            route = Screens.Statistics.route + "/{noteId}/{levelId}/{score}/{attemptCount}/{totalNumberOfQuestions}/{stage}/{isRevision}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("levelId") {
                    type = NavType.LongType
                    defaultValue = 0
                },
                navArgument("score") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("attemptCount") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("totalNumberOfQuestions") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("stage") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("isRevision") {
                    type = NavType.BoolType
                    defaultValue = false // Your parameter goes here
                },
            )
        ) {
            Statistics(navController = navController)
        }

        composable(
            route = Screens.Upgrade.route,
        ) {
            Upgrade(navController = navController)
        }

    }

}