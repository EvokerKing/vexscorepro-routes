package com.vexscores.pro.routes.ui

import android.util.Log
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.vexscores.pro.routes.MainActivity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit.DAYS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(controller: NavController, context: MainActivity, item: String) {
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text(item) },
				navigationIcon = {
					IconButton(onClick = { controller.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
				}
			)
		}
	) { innerPadding ->
		if (item == "Events List") {
			val list = remember { mutableStateListOf<Map<String, Any?>?>(null) }
			LaunchedEffect(Unit) {
				val res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/events/list")
	            val data = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
	            list.clear()
	            data.forEach {
	                list += (it as JsonObject).toMap()
	            }
			}
			if (list.isEmpty()) {
				FlowColumn(
					verticalArrangement = Arrangement.Center,
					horizontalArrangement = Arrangement.Center,
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding)
				) {
					Text("No results returned")
				}
			} else if (list.first() == null) {
				FlowColumn(
					verticalArrangement = Arrangement.Center,
					horizontalArrangement = Arrangement.Center,
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding)
				) {
					CircularProgressIndicator()
				}
			} else {
				LazyColumn(modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding)) {
					list.forEach { item ->
						item {
							ElevatedCard(modifier = Modifier
								.fillMaxWidth()
								.padding(8.dp)) {
								Text(
									item?.get("id")?.toString() ?: "UNKNOWN ID",
									style = MaterialTheme.typography.labelSmall,
									color = Color(0xFF888888),
									modifier = Modifier.padding(8.dp)
								)
								Text(
									item?.get("name") as? String ?: "UNKNOWN NAME",
									style = MaterialTheme.typography.headlineSmall,
									modifier = Modifier.padding(start = 8.dp)
								)
								val start = Instant.parse(item?.get("start") as? String ?: "1970-01-01T00:00:00-05:00").atZone(ZoneId.systemDefault())
								val end = Instant.parse(item?.get("end") as? String ?: "1970-01-01T00:00:00-05:00").atZone(ZoneId.systemDefault())
								if (start == end) {
									Text(
										start.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)),
										style = MaterialTheme.typography.bodySmall,
										modifier = Modifier.padding(start = 8.dp)
									)
								} else {
									Text(
										"${start.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))} - ${end.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))}",
										style = MaterialTheme.typography.bodySmall,
										modifier = Modifier.padding(start = 8.dp)
									)
								}
								Text(
									(item?.get("season") as? Map<*, *>)?.get("name") as? String ?: "UNKNOWN SEASON",
									style = MaterialTheme.typography.bodySmall,
									modifier = Modifier.padding(start = 8.dp)
								)
								Text(
									(item?.get("location") as? Map<*, *>)?.get("venue") as? String ?: "UNKNOWN LOCATION",
									style = MaterialTheme.typography.bodySmall,
									modifier = Modifier.padding(start = 8.dp)
								)
								Text(
									if (item?.get("awards_finalized") as? Boolean == true) { "Event Done" } else if (item?.get("awards_finalized") as? Boolean == false) { "Event Ongoing or Upcoming" } else { "UNKNOWN FINALIZED STATE" },
									style = MaterialTheme.typography.titleMedium,
									modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
								)
							}
						}
					}
				}
			}
		}
	}
}