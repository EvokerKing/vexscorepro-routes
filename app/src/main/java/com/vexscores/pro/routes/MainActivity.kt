package com.vexscores.pro.routes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vexscores.pro.routes.ui.ListScreen
import com.vexscores.pro.routes.ui.RouteScreen
import com.vexscores.pro.routes.ui.routes

object AppDestinations {
	const val LIST = "list of routes"
}

class MainActivity : ComponentActivity() {
	lateinit var scheme: ColorScheme

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		enableEdgeToEdge()
		setContent {
			scheme = if (isSystemInDarkTheme()) dynamicDarkColorScheme(applicationContext) else dynamicLightColorScheme(applicationContext)

			MaterialTheme(
				colorScheme = scheme
			) {
				Controller(this)
			}
		}
	}
}

@Composable
fun Controller(context: MainActivity) {
	val navController = rememberNavController()

	NavHost(
		navController = navController,
		startDestination = AppDestinations.LIST,
		modifier = Modifier.fillMaxSize()
	) {
		composable(AppDestinations.LIST) {
			ListScreen(navController, context)
		}

		routes.forEach { i ->
			composable(i.key) {
				RouteScreen(navController, i.key)
			}
		}
	}
}