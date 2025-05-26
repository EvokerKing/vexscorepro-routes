package com.vexscores.pro.routes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vexscores.pro.routes.ui.ListScreen

object AppDestinations {
	const val LIST = "list of routes"
}

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		enableEdgeToEdge()
		setContent {
			MaterialTheme(
				colorScheme = if (isSystemInDarkTheme()) dynamicDarkColorScheme(applicationContext) else dynamicLightColorScheme(applicationContext)
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
			ListScreen(navController)
		}
	}
}