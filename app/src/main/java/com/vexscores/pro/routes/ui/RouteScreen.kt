package com.vexscores.pro.routes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(controller: NavController, item: String) {
	val scope = rememberCoroutineScope()

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
			var filters = remember { mutableStateListOf("", "", "", "", "") }
			val list = remember { mutableStateListOf<Map<String, Any?>?>(null) }
			val games = remember { mutableStateListOf<String?>(null) }
			val states = listOf("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming")
			val levels = listOf("World", "National", "Regional", "Signature", "Other")
			suspend fun update() {
				list.clear()
				list += null
				var searchParams = ""
				if (!filters[0].isEmpty()) {
					searchParams += "&team=${URLEncoder.encode(filters[0], "UTF-8")}"
				}
				if (!filters[1].isEmpty()) {
					searchParams += "&game=${URLEncoder.encode(filters[1], "UTF-8")}"
				}
				if (!filters[2].isEmpty()) {
					searchParams += "&start=${URLEncoder.encode(filters[2].split(":")[0], "UTF-8")}"
					searchParams += "&end=${URLEncoder.encode(filters[2].split(":")[1], "UTF-8")}"
				}
				if (!filters[3].isEmpty()) {
					searchParams += "&region=${URLEncoder.encode(filters[3], "UTF-8")}"
				}
				if (!filters[4].isEmpty()) {
					searchParams += "&level=${URLEncoder.encode(filters[4], "UTF-8")}"
				}
				var res: HttpResponse
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/events/list?pages=1$searchParams")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				val data = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
				list.clear()
				data.forEach {
					list += (it as JsonObject).toMap()
				}
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/seasons")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				val gameData = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
				games.clear()
				gameData.forEach {
					var name = ((it as JsonObject).toMap()["name"]!! as String)
					if (": " in name) {
						name = name.split(": ")[1]
					}
					if (name !in games) {
						games += name
					}
				}
			}
			LaunchedEffect(Unit) {
				update()
			}
			var showTeamDialog = remember { mutableStateOf(false) }
			var showGameMenu = remember { mutableStateOf(false) }
			var showDateDialog = remember { mutableStateOf(false) }
			var showStateMenu = remember { mutableStateOf(false) }
			var showLevelMenu = remember { mutableStateOf(false) }
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)) {
				Row(modifier = Modifier
					.padding(4.dp)
					.horizontalScroll(rememberScrollState())) {
					InputChip(
						onClick = {
							showTeamDialog.value = true
						},
						label = { Text("Team Number${if (!filters[0].isEmpty()) ": ${filters[0]}" else ""}") },
						selected = !filters[0].isEmpty(),
						trailingIcon = {
							if (!filters[0].isEmpty()) {
								IconButton(
									onClick = {
										filters[0] = ""
										scope.launch {
											update()
										}
									},
									modifier = Modifier.size(InputChipDefaults.IconSize)
								) {
									Icon(Icons.Rounded.Clear, "Clear")
								}
							}
						},
						modifier = Modifier.padding(4.dp)
					)
					Box {
						InputChip(
							onClick = {
								showGameMenu.value = true
							},
							label = { Text("Game${if (!filters[1].isEmpty()) ": ${filters[1]}" else ""}") },
							selected = !filters[1].isEmpty(),
							trailingIcon = {
								if (!filters[1].isEmpty()) {
									IconButton(
										onClick = {
											filters[1] = ""
											scope.launch {
												update()
											}
										},
										modifier = Modifier.size(InputChipDefaults.IconSize)
									) {
										Icon(Icons.Rounded.Clear, "Clear")
									}
								}
							},
							modifier = Modifier.padding(4.dp)
						)
						DropdownMenu(
							expanded = showGameMenu.value,
							onDismissRequest = { showGameMenu.value = false },
							modifier = Modifier
								.height(500.dp)
								.width(300.dp)
						) {
							if (games.first() == null) {
								FlowColumn(
									modifier = Modifier.fillMaxSize(),
									horizontalArrangement = Arrangement.Center,
									verticalArrangement = Arrangement.Center
								) {
									CircularProgressIndicator()
								}
							} else {
								games.forEach {
									DropdownMenuItem(
										text = { Text(it!!) },
										onClick = {
											filters[1] = it!!
											scope.launch {
												update()
											}
											showGameMenu.value = false
										}
									)
								}
							}
						}
					}
					InputChip(
						onClick = {
							showDateDialog.value = true
						},
						label = { Text("Dates${if (!filters[2].isEmpty()) ": ${filters[2]}" else ""}") },
						selected = !filters[2].isEmpty(),
						trailingIcon = {
							if (!filters[2].isEmpty()) {
								IconButton(
									onClick = {
										filters[2] = ""
										scope.launch {
											update()
										}
									},
									modifier = Modifier.size(InputChipDefaults.IconSize)
								) {
									Icon(Icons.Rounded.Clear, "Clear")
								}
							}
						},
						modifier = Modifier.padding(4.dp)
					)
					Box {
						InputChip(
							onClick = {
								showStateMenu.value = true
							},
							label = { Text("State${if (!filters[3].isEmpty()) ": ${filters[3]}" else ""}") },
							selected = !filters[3].isEmpty(),
							trailingIcon = {
								if (!filters[3].isEmpty()) {
									IconButton(
										onClick = {
											filters[3] = ""
											scope.launch {
												update()
											}
										},
										modifier = Modifier.size(InputChipDefaults.IconSize)
									) {
										Icon(Icons.Rounded.Clear, "Clear")
									}
								}
							},
							modifier = Modifier.padding(4.dp)
						)
						DropdownMenu(
							expanded = showStateMenu.value,
							onDismissRequest = { showStateMenu.value = false },
							modifier = Modifier
								.height(500.dp)
								.width(300.dp)
						) {
							states.forEach {
								DropdownMenuItem(
									text = { Text(it) },
									onClick = {
										filters[3] = it
										scope.launch {
											update()
										}
										showStateMenu.value = false
									}
								)
							}
						}
					}
					Box {
						InputChip(
							onClick = {
								showLevelMenu.value = true
							},
							label = { Text("Level${if (!filters[4].isEmpty()) ": ${filters[4]}" else ""}") },
							selected = !filters[4].isEmpty(),
							trailingIcon = {
								if (!filters[4].isEmpty()) {
									IconButton(
										onClick = {
											filters[4] = ""
											scope.launch {
												update()
											}
										},
										modifier = Modifier.size(InputChipDefaults.IconSize)
									) {
										Icon(Icons.Rounded.Clear, "Clear")
									}
								}
							},
							modifier = Modifier.padding(4.dp)
						)
						DropdownMenu(
							expanded = showLevelMenu.value,
							onDismissRequest = { showLevelMenu.value = false },
							modifier = Modifier.width(300.dp)
						) {
							levels.forEach {
								DropdownMenuItem(
									text = { Text(it) },
									onClick = {
										filters[4] = it
										scope.launch {
											update()
										}
										showLevelMenu.value = false
									}
								)
							}
						}
					}
				}
				if (showTeamDialog.value) {
					var current = remember { mutableStateOf(filters[0]) }
					Dialog(
						onDismissRequest = {
							showTeamDialog.value = false
						}
					) {
						Card(
							modifier = Modifier
								.fillMaxWidth()
								.height(200.dp),
							shape = RoundedCornerShape(16.dp)
						) {
							FlowColumn(
								modifier = Modifier.fillMaxSize(),
								verticalArrangement = Arrangement.SpaceEvenly
							) {
								Text(
									"Enter Team Number",
									modifier = Modifier.align(Alignment.CenterHorizontally),
									style = MaterialTheme.typography.headlineSmall
								)
								OutlinedTextField(
									value = current.value,
									onValueChange = { new: String ->
										if (new.matches(Regex("[a-zA-Z0-9]{0,6}"))) {
											current.value = new.uppercase()
										}
									},
									label = { Text("Team Number") },
									modifier = Modifier
										.padding(horizontal = 16.dp)
										.fillMaxWidth()
								)
								Row(
									modifier = Modifier
										.align(Alignment.End)
										.padding(horizontal = 16.dp)
								) {
									TextButton(
										onClick = {
											showTeamDialog.value = false
										}
									) {
										Text("Dismiss")
									}
									TextButton(
										onClick = {
											filters[0] = current.value
											scope.launch {
												update()
											}
											showTeamDialog.value = false
										},
										enabled = current.value.matches(Regex("^([a-zA-Z]{1,5})$|^([0-9]{1,5}[a-zA-z])$"))
									) {
										Text("Confirm")
									}
								}
							}
						}
					}
				}
				if (showDateDialog.value) {
					val current = rememberDateRangePickerState()
					DatePickerDialog(
						onDismissRequest = {
							showDateDialog.value = false
						},
						confirmButton = {
							TextButton(
								onClick = {
									current.selectedStartDateMillis ?: return@TextButton
									current.selectedEndDateMillis ?: return@TextButton
									val start = Instant.ofEpochMilli(current.selectedStartDateMillis!!.toLong()).atZone(ZoneId.systemDefault())
									val end = Instant.ofEpochMilli(current.selectedEndDateMillis!!.toLong()).atZone(ZoneId.systemDefault())
									filters[2] = "${start.year}-${start.monthValue}-${start.dayOfMonth}:${end.year}-${end.monthValue}-${end.dayOfMonth}"
									scope.launch {
										update()
									}
									showDateDialog.value = false
								},
								enabled = current.selectedStartDateMillis != null && current.selectedEndDateMillis != null
							) {
								Text("Confirm")
							}
						},
						dismissButton = {
							TextButton(
								onClick = {
									showDateDialog.value = false
								}
							) {
								Text("Dismiss")
							}
						}
					) {
						DateRangePicker(
							state = current,
							title = {
								Text("Select date range")
							},
							showModeToggle = false,
							modifier = Modifier
								.fillMaxWidth()
								.height(500.dp)
								.padding(16.dp)
						)
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
					LazyColumn(modifier = Modifier.fillMaxSize()) {
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
										if (item?.get("awards_finalized") as? Boolean == true) "Event Done" else if (item?.get("awards_finalized") as? Boolean == false) "Event Ongoing or Upcoming" else "UNKNOWN FINALIZED STATE",
										style = MaterialTheme.typography.titleMedium,
										modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
									)
								}
							}
						}
					}
				}
			}
		} else if (item == "Event Info") {
			var event = remember { mutableStateOf("") }
			var info = remember { mutableStateMapOf<Any?, Any?>(null to null) }
			suspend fun update() {
				if (event.value.isEmpty()) { return }
				info.clear()
				info.put("loading", null)
				val res: HttpResponse
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/events/info?id=${URLEncoder.encode(event.value, "UTF-8")}")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				val data = Parser.default().parse(StringBuilder(res.body<String>())) as JsonObject
				info.clear()
				info.putAll(data.toMap() as Map<*, *>)
			}
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)) {
				OutlinedTextField(
					value = event.value,
					onValueChange = { new: String ->
						if (new.matches(Regex("[0-9]{0,6}"))) {
							event.value = new
							scope.launch {
								update()
							}
						}
					},
					label = { Text("Event ID") },
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
				)
				if (info.keys.first() == null) {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Type in an event ID to get the info of it")
					}
				} else if (info["message"] == "The event could not be found.") {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Event could not be found")
					}
				} else if (info.keys.first() == "loading") {
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
					Column(modifier = Modifier
						.fillMaxSize()
						.padding(8.dp)) {
						Text(
							info["name"] as String,
							style = MaterialTheme.typography.headlineLarge,
							modifier = Modifier
								.padding(8.dp)
								.fillMaxWidth()
								.padding(top = 16.dp, bottom = 32.dp),
							textAlign = TextAlign.Center
						)
						Text(
							if (info["ongoing"] as Boolean) "Event Ongoing" else if (info["awards_finalized"] as Boolean) "Event Finalized" else "Unknown Event State",
							style = MaterialTheme.typography.bodyLarge,
							modifier = Modifier.align(Alignment.CenterHorizontally)
						)
						ElevatedCard(modifier = Modifier.padding(16.dp)) {
							FlowColumn(modifier = Modifier
								.padding(16.dp)
								.fillMaxWidth()) {
								Text(
									"Competition:",
									style = MaterialTheme.typography.titleMedium
								)
								Text(
									(info["program"] as Map<*, *>)["name"] as String,
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.fillMaxWidth(),
									textAlign = TextAlign.Center
								)
								Text(
									((info["season"] as Map<*, *>)["name"] as String).split(": ")[1],
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.fillMaxWidth(),
									textAlign = TextAlign.Center
								)
							}
						}
						Text(
							"Level: ${info["level"] as String}",
							style = MaterialTheme.typography.bodyLarge,
							modifier = Modifier.align(Alignment.CenterHorizontally)
						)
						ElevatedCard(modifier = Modifier.padding(16.dp)) {
							FlowColumn(modifier = Modifier
								.padding(16.dp)
								.fillMaxWidth()) {
								Text(
									"Location:",
									style = MaterialTheme.typography.titleMedium
								)
								Text(
									(info["location"] as Map<*, *>)["venue"] as String,
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.fillMaxWidth(),
									textAlign = TextAlign.Center
								)
								Text(
									(info["location"] as Map<*, *>)["address_1"] as String,
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.fillMaxWidth(),
									textAlign = TextAlign.Center
								)
								if ((info["location"] as Map<*, *>)["address_2"] != null) {
									Text(
										(info["location"] as Map<*, *>)["address_2"] as String,
										style = MaterialTheme.typography.titleLarge,
										modifier = Modifier.fillMaxWidth(),
										textAlign = TextAlign.Center
									)
								}
								Text(
									"${(info["location"] as Map<*, *>)["city"] as String}, ${(info["location"] as Map<*, *>)["region"] as String} ${(info["location"] as Map<*, *>)["postcode"] as String}",
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.fillMaxWidth(),
									textAlign = TextAlign.Center
								)
								Text(
									(info["location"] as Map<*, *>)["country"] as String,
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.fillMaxWidth(),
									textAlign = TextAlign.Center
								)
							}
						}
						val start = Instant.parse(info["start"] as String).atZone(ZoneId.systemDefault())
						val end = Instant.parse(info["end"] as String).atZone(ZoneId.systemDefault())
						if (start == end) {
							Text(
								start.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
								style = MaterialTheme.typography.bodyLarge,
								modifier = Modifier.align(Alignment.CenterHorizontally)
							)
						} else {
							Text(
								"${start.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))} - ${end.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))}",
								style = MaterialTheme.typography.bodyLarge,
								modifier = Modifier.align(Alignment.CenterHorizontally)
							)
						}
						ElevatedCard(modifier = Modifier.padding(16.dp)) {
							FlowColumn(modifier = Modifier
								.padding(16.dp)
								.fillMaxWidth()) {
								Text(
									"Divisions:",
									style = MaterialTheme.typography.titleMedium
								)
								(info["divisions"] as List<*>).forEach {
									Text(
										"${(it as Map<*, *>)["id"] as Int}: ${it["name"] as String}",
										style = MaterialTheme.typography.titleLarge,
										modifier = Modifier.fillMaxWidth(),
										textAlign = TextAlign.Center
									)
								}

							}
						}
					}
				}
			}
		} else if (item == "Event Teams") {
			var event = remember { mutableStateOf("") }
			var teams = remember { mutableStateListOf<Any?>(null) }
			suspend fun update() {
				if (event.value.isEmpty()) { return }
				teams.clear()
				teams += "loading"
				val res: HttpResponse
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/events/teams?id=${URLEncoder.encode(event.value, "UTF-8")}")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				if (res.status.value == 500) {
					teams.clear()
					teams += "not found"
					return
				}
				val data = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
				teams.clear()
				data.forEach {
					teams += it
				}
			}
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)) {
				OutlinedTextField(
					value = event.value,
					onValueChange = { new: String ->
						if (new.matches(Regex("[0-9]{0,6}"))) {
							event.value = new
							scope.launch {
								update()
							}
						}
					},
					label = { Text("Event ID") },
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
				)
				if (teams.first() == null) {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Type in an event ID to get the teams who competed in it")
					}
				} else if (teams.first() == "not found") {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Event could not be found")
					}
				} else if (teams.first() == "loading") {
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
					LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
						teams.forEachIndexed { index, team ->
							item {
								ListItem(
									headlineContent = { Text("${(team as JsonObject)["number"] as String} - ${team["team_name"] as String}") },
									supportingContent = { Text("${(team as JsonObject)["organization"] as String} (${(team["location"] as JsonObject)["city"] as String}, ${(team["location"] as JsonObject)["region"] as String})") },
									trailingContent = { Text(((team as JsonObject)["id"] as Integer).toString()) }
								)
								if (index != teams.size) {
									HorizontalDivider()
								}
							}
						}
					}
				}
			}
		} else if (item == "Event Skills") {
			var event = remember { mutableStateOf("") }
			var runs = remember { mutableStateListOf<Any?>(null) }
			suspend fun update() {
				if (event.value.isEmpty()) { return }
				runs.clear()
				runs += "loading"
				val res: HttpResponse
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/events/skills?id=${URLEncoder.encode(event.value, "UTF-8")}")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				if (res.status.value == 500) {
					runs.clear()
					runs += "not found"
					return
				}
				val data = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
				runs.clear()
				data.forEach {
					runs += it
				}
			}
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)) {
				OutlinedTextField(
					value = event.value,
					onValueChange = { new: String ->
						if (new.matches(Regex("[0-9]{0,6}"))) {
							event.value = new
							scope.launch {
								update()
							}
						}
					},
					label = { Text("Event ID") },
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
				)
				if (runs.first() == null) {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Type in an event ID to get the skills runs from it")
					}
				} else if (runs.first() == "not found") {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Event could not be found")
					}
				} else if (runs.first() == "loading") {
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
					LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
						runs.forEachIndexed { index, run ->
							item {
								val attempts = (run as JsonObject)["attempts"] as Int
								ListItem(
									headlineContent = { Text((run["team"] as JsonObject)["name"] as String) },
									supportingContent = { Text("${(run["type"] as String).capitalize(Locale.getDefault())} - $attempts attempt${if (attempts != 1) "s" else ""}") },
									trailingContent = { Text("${run["score"] as Int} points") },
									leadingContent = { Text("#${run["rank"] as Int}") }
								)
								if (index != runs.size) {
									HorizontalDivider()
								}
							}
						}
					}
				}
			}
		} else if (item == "Event Awards") {
			var event = remember { mutableStateOf("") }
			var awards = remember { mutableStateListOf<Any?>(null) }
			suspend fun update() {
				if (event.value.isEmpty()) {
					return
				}
				awards.clear()
				awards += "loading"
				val res: HttpResponse
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/events/awards?id=${URLEncoder.encode(event.value, "UTF-8")}")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				val body = res.body<String>()
				if (body.isEmpty()) {
					awards.clear()
					awards += "not found"
					return
				}
				val data = Parser.default().parse(StringBuilder(body)) as JsonArray<*>
				awards.clear()
				data.forEach {
					awards += it
				}
			}
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)) {
				OutlinedTextField(
					value = event.value,
					onValueChange = { new: String ->
						if (new.matches(Regex("[0-9]{0,6}"))) {
							event.value = new
							scope.launch {
								update()
							}
						}
					},
					label = { Text("Event ID") },
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
				)
				if (awards.first() == null) {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Type in an event ID to get the awards from it")
					}
				} else if (awards.first() == "not found") {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Event could not be found")
					}
				} else if (awards.first() == "loading") {
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
					Column(modifier = Modifier
						.padding(top = 16.dp)
						.verticalScroll(rememberScrollState())) {
						awards.forEach {
							it as? JsonObject ?: return@forEach
							ElevatedCard(modifier = Modifier
								.fillMaxWidth()
								.padding(8.dp)) {
								Text(
									it["title"] as String,
									style = MaterialTheme.typography.headlineSmall,
									modifier = Modifier.padding(8.dp)
								)
								Text(
									"Awarded to:",
									style = MaterialTheme.typography.titleMedium,
									modifier = Modifier.padding(start = 8.dp)
								)
								(it["teamWinners"] as JsonArray<*>).forEach { team ->
									team as? JsonObject ?: return@forEach
									Text(
										"${(team["team"] as JsonObject)["name"] as String} - ${(team["division"] as JsonObject)["name"] as String}",
										style = MaterialTheme.typography.titleLarge,
										modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
									)
								}
							}
						}
					}
				}
			}
		} else if (item == "Event Division Rankings") {
			var event = remember { mutableStateOf("") }
			var division = remember { mutableStateOf("") }
			var teams = remember { mutableStateListOf<Any?>(null) }
			suspend fun update() {
				if (event.value.isEmpty() || division.value.isEmpty()) {
					return
				}
				teams.clear()
				teams += "loading"
				val res: HttpResponse
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/events/rankings?id=${URLEncoder.encode(event.value, "UTF-8")}&div=${URLEncoder.encode(division.value, "UTF-8")}")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				if (res.status.value == 500) {
					teams.clear()
					teams += "not found"
					return
				}
				val data = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
				if (data.isEmpty()) {
					teams.clear()
					teams += "div not found"
					return
				}
				teams.clear()
				data.forEach {
					teams += it
				}
				teams.reverse()
			}
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)) {
				Row {
					OutlinedTextField(
						value = event.value,
						onValueChange = { new: String ->
							if (new.matches(Regex("[0-9]{0,6}"))) {
								event.value = new
								scope.launch {
									update()
								}
							}
						},
						label = { Text("Event ID") },
						modifier = Modifier
							.padding(horizontal = 16.dp)
							.fillMaxWidth(0.6f),
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
					)
					OutlinedTextField(
						value = division.value,
						onValueChange = { new: String ->
							if (new.matches(Regex("[0-9]{0,6}"))) {
								division.value = new
								scope.launch {
									update()
								}
							}
						},
						label = { Text("Division ID") },
						modifier = Modifier
							.padding(horizontal = 16.dp)
							.fillMaxWidth(),
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
					)
				}
				if (teams.first() == null) {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Type in an event and division ID to get the rankings from it")
					}
				} else if (teams.first() == "not found") {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Event could not be found")
					}
				} else if (teams.first() == "div not found") {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Division could not be found")
					}
				} else if (teams.first() == "loading") {
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
					Column(modifier = Modifier
						.padding(top = 16.dp)
						.verticalScroll(rememberScrollState())) {
						teams.forEachIndexed { index, team ->
							team as? JsonObject ?: return@forEachIndexed
							ListItem(
								headlineContent = { Text("${(team["team"] as JsonObject)["name"]} (${team["wins"] as Int}-${team["losses"] as Int}-${team["ties"] as Int})") },
								supportingContent = { Text("${team["wp"] as Int} WPs - ${team["ap"] as Int} APs - ${team["sp"] as Int} SPs") },
								leadingContent = { Text((team["rank"] as Int).toString()) }
							)
							if (index + 1 != teams.size) {
								HorizontalDivider()
							}
						}
					}
				}
			}
		} else if (item == "Event Division Matches") {
			var event = remember { mutableStateOf("") }
			var division = remember { mutableStateOf("") }
			var matches = remember { mutableStateListOf<Any?>(null) }
			suspend fun update() {
				if (event.value.isEmpty() || division.value.isEmpty()) {
					return
				}
				matches.clear()
				matches += "loading"
				val res: HttpResponse
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/events/matches?id=${URLEncoder.encode(event.value, "UTF-8")}&div=${URLEncoder.encode(division.value, "UTF-8")}")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				if (res.status.value == 500) {
					matches.clear()
					matches += "not found"
					return
				}
				val data = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
				if (data.isEmpty()) {
					matches.clear()
					matches += "div not found"
					return
				}
				matches.clear()
				data.forEach {
					matches += it
				}
				matches.reverse()
			}
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)) {
				Row {
					OutlinedTextField(
						value = event.value,
						onValueChange = { new: String ->
							if (new.matches(Regex("[0-9]{0,6}"))) {
								event.value = new
								scope.launch {
									update()
								}
							}
						},
						label = { Text("Event ID") },
						modifier = Modifier
							.padding(horizontal = 16.dp)
							.fillMaxWidth(0.6f),
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
					)
					OutlinedTextField(
						value = division.value,
						onValueChange = { new: String ->
							if (new.matches(Regex("[0-9]{0,6}"))) {
								division.value = new
								scope.launch {
									update()
								}
							}
						},
						label = { Text("Division ID") },
						modifier = Modifier
							.padding(horizontal = 16.dp)
							.fillMaxWidth(),
						keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
					)
				}
				if (matches.first() == null) {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Type in an event and division ID to get the matches from it")
					}
				} else if (matches.first() == "not found") {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Event could not be found")
					}
				} else if (matches.first() == "div not found") {
					FlowColumn(
						verticalArrangement = Arrangement.Center,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.fillMaxSize()
							.padding(innerPadding)
					) {
						Text("Division could not be found or no matches are posted")
					}
				} else if (matches.first() == "loading") {
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
					Column(modifier = Modifier
						.padding(top = 16.dp)
						.verticalScroll(rememberScrollState())) {
						matches.forEachIndexed { index, match ->
							match as? JsonObject ?: return@forEachIndexed
							val redPoints = ((match["alliances"] as JsonArray<*>)[1] as JsonObject)["score"] as? Int ?: -1
							val bluePoints = ((match["alliances"] as JsonArray<*>)[0] as JsonObject)["score"] as? Int ?: -1
							Text(
								match["name"] as String,
								modifier = Modifier.fillMaxWidth(),
								textAlign = TextAlign.Center
							)
							FlowRow(
								horizontalArrangement = Arrangement.SpaceEvenly,
								modifier = Modifier.fillMaxWidth()
							) {
								Box(
									modifier = Modifier.background(Color(0xBFCB0000), RoundedCornerShape(4.dp))
								) {
									Column(
										modifier = Modifier.padding(4.dp)
									) {
										val team1 = ((((match["alliances"] as JsonArray<*>)[1] as JsonObject)["teams"] as JsonArray<*>)[0] as JsonObject)["team"] as JsonObject
										val team2 = ((((match["alliances"] as JsonArray<*>)[1] as JsonObject)["teams"] as JsonArray<*>)[1] as JsonObject)["team"] as JsonObject
										Text(
											team1["name"] as String,
											modifier = Modifier.align(Alignment.CenterHorizontally),
											textDecoration = if (redPoints > bluePoints) TextDecoration.Underline else TextDecoration.None
										)
										Text(
											team2["name"] as String,
											modifier = Modifier.align(Alignment.CenterHorizontally),
											textDecoration = if (redPoints > bluePoints) TextDecoration.Underline else TextDecoration.None
										)
									}
								}
								if (redPoints == -1 || bluePoints == -1) {
									val time = Instant.parse(match["started"] as String).atZone(ZoneId.systemDefault())
									Text("${time.hour}:${time.minute}")
								} else {
									Text(
										redPoints.toString(),
										style = MaterialTheme.typography.titleLarge,
										modifier = Modifier.align(Alignment.CenterVertically),
										textDecoration = if (redPoints > bluePoints) TextDecoration.Underline else TextDecoration.None
									)
									Text(
										bluePoints.toString(),
										style = MaterialTheme.typography.titleLarge,
										modifier = Modifier.align(Alignment.CenterVertically),
										textDecoration = if (bluePoints > redPoints) TextDecoration.Underline else TextDecoration.None
									)
								}
								Box(
									modifier = Modifier.background(Color(0xBF0000CB), RoundedCornerShape(4.dp))
								) {
									Column(
										modifier = Modifier.padding(4.dp)
									) {
										val team1 = ((((match["alliances"] as JsonArray<*>)[0] as JsonObject)["teams"] as JsonArray<*>)[0] as JsonObject)["team"] as JsonObject
										val team2 = ((((match["alliances"] as JsonArray<*>)[0] as JsonObject)["teams"] as JsonArray<*>)[1] as JsonObject)["team"] as JsonObject
										Text(
											team1["name"] as String,
											modifier = Modifier.align(Alignment.CenterHorizontally),
											textDecoration = if (bluePoints > redPoints) TextDecoration.Underline else TextDecoration.None
										)
										Text(
											team2["name"] as String,
											modifier = Modifier.align(Alignment.CenterHorizontally),
											textDecoration = if (bluePoints > redPoints) TextDecoration.Underline else TextDecoration.None
										)
									}
								}
							}
							val field = match["field"] as? String ?: "UNKNOWN FIELD"
							Text(
								"$field${if ("field" !in field.lowercase()) " Field" else ""}",
								modifier = Modifier.fillMaxWidth(),
								textAlign = TextAlign.Center
							)
							if (index + 1 != matches.size) {
								HorizontalDivider(modifier = Modifier.padding(8.dp))
							}
						}
					}
				}
			}
		} else if (item == "Teams List") {
			var filters = remember { mutableStateListOf("", "0", "", "") }
			val list = remember { mutableStateListOf<Map<String, Any?>?>(null) }
			val programs = remember { mutableStateListOf<String?>(null) }
			val grades = listOf("College", "High School", "Middle School", "Elementary School")
			suspend fun update() {
				list.clear()
				list += null
				var searchParams = ""
				if (!filters[0].isEmpty()) {
					searchParams += "&event=${filters[0]}"
				}
				if (filters[1] == "1") {
					searchParams += "&registered=true"
				}
				if (!filters[2].isEmpty()) {
					searchParams += "&program=${filters[2]}"
				}
				if (!filters[3].isEmpty()) {
					searchParams += "&grade=${URLEncoder.encode(filters[3], "UTF-8")}"
				}
				var res: HttpResponse
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/teams/list?pages=1$searchParams")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				val data = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
				list.clear()
				data.forEach {
					list += (it as JsonObject).toMap()
				}
				try {
					res = HttpClient(CIO).get("https://vexscorepro.onrender.com/api/programs")
				} catch (_: HttpRequestTimeoutException) {
					update()
					return
				}
				val programData = Parser.default().parse(StringBuilder(res.body<String>())) as JsonArray<*>
				programs.clear()
				programData.forEach {
					var name = ((it as JsonObject).toMap()["abbr"]!! as String)
					if (name !in programs) {
						programs += name
					}
				}
			}
			LaunchedEffect(Unit) {
				update()
			}
			var showEventDialog = remember { mutableStateOf(false) }
			var showProgramMenu = remember { mutableStateOf(false) }
			var showGradeMenu = remember { mutableStateOf(false) }
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)) {
				Row(modifier = Modifier
					.padding(4.dp)
					.horizontalScroll(rememberScrollState())) {
					InputChip(
						onClick = {
							showEventDialog.value = true
						},
						label = { Text("Event ID${if (!filters[0].isEmpty()) ": ${filters[0]}" else ""}") },
						selected = !filters[0].isEmpty(),
						trailingIcon = {
							if (!filters[0].isEmpty()) {
								IconButton(
									onClick = {
										filters[0] = ""
										scope.launch {
											update()
										}
									},
									modifier = Modifier.size(InputChipDefaults.IconSize)
								) {
									Icon(Icons.Rounded.Clear, "Clear")
								}
							}
						},
						modifier = Modifier.padding(4.dp)
					)
					FilterChip(
						onClick = {
							if (filters[1] == "0") {
								filters[1] = "1"
								scope.launch {
									update()
								}
							} else {
								filters[1] = "0"
								scope.launch {
									update()
								}
							}
						},
						label = { Text("Registered Only") },
						selected = filters[1] == "1",
						leadingIcon = if (filters[1] == "1") {
							{
								Icon(
									imageVector = Icons.Rounded.Done,
									contentDescription = "Selected",
									modifier = Modifier.size(FilterChipDefaults.IconSize)
								)
							}
						} else {
							null
						},
						modifier = Modifier.padding(4.dp)
					)
					Box {
						InputChip(
							onClick = {
								showProgramMenu.value = true
							},
							label = { Text("Program${if (!filters[2].isEmpty()) ": ${filters[2]}" else ""}") },
							selected = !filters[2].isEmpty(),
							trailingIcon = {
								if (!filters[2].isEmpty()) {
									IconButton(
										onClick = {
											filters[2] = ""
											scope.launch {
												update()
											}
										},
										modifier = Modifier.size(InputChipDefaults.IconSize)
									) {
										Icon(Icons.Rounded.Clear, "Clear")
									}
								}
							},
							modifier = Modifier.padding(4.dp)
						)
						DropdownMenu(
							expanded = showProgramMenu.value,
							onDismissRequest = { showProgramMenu.value = false },
							modifier = Modifier
								.width(300.dp)
						) {
							if (programs.first() == null) {
								FlowColumn(
									modifier = Modifier.fillMaxSize(),
									horizontalArrangement = Arrangement.Center,
									verticalArrangement = Arrangement.Center
								) {
									CircularProgressIndicator()
								}
							} else {
								programs.forEach {
									DropdownMenuItem(
										text = { Text(it!!) },
										onClick = {
											filters[2] = it!!
											scope.launch {
												update()
											}
											showProgramMenu.value = false
										}
									)
								}
							}
						}
					}
					Box {
						InputChip(
							onClick = {
								showGradeMenu.value = true
							},
							label = { Text("Grade${if (!filters[3].isEmpty()) ": ${filters[3]}" else ""}") },
							selected = !filters[3].isEmpty(),
							trailingIcon = {
								if (!filters[3].isEmpty()) {
									IconButton(
										onClick = {
											filters[3] = ""
											scope.launch {
												update()
											}
										},
										modifier = Modifier.size(InputChipDefaults.IconSize)
									) {
										Icon(Icons.Rounded.Clear, "Clear")
									}
								}
							},
							modifier = Modifier.padding(4.dp)
						)
						DropdownMenu(
							expanded = showGradeMenu.value,
							onDismissRequest = { showGradeMenu.value = false },
							modifier = Modifier
								.width(300.dp)
						) {
							grades.forEach {
								DropdownMenuItem(
									text = { Text(it) },
									onClick = {
										filters[3] = it
										scope.launch {
											update()
										}
										showGradeMenu.value = false
									}
								)
							}
						}
					}
				}
				if (showEventDialog.value) {
					var current = remember { mutableStateOf(filters[0]) }
					Dialog(
						onDismissRequest = {
							showEventDialog.value = false
						}
					) {
						Card(
							modifier = Modifier
								.fillMaxWidth()
								.height(200.dp),
							shape = RoundedCornerShape(16.dp)
						) {
							FlowColumn(
								modifier = Modifier.fillMaxSize(),
								verticalArrangement = Arrangement.SpaceEvenly
							) {
								Text(
									"Enter Event ID",
									modifier = Modifier.align(Alignment.CenterHorizontally),
									style = MaterialTheme.typography.headlineSmall
								)
								OutlinedTextField(
									value = current.value,
									onValueChange = { new: String ->
										if (new.matches(Regex("[0-9]{0,6}"))) {
											current.value = new.uppercase()
										}
									},
									label = { Text("Event ID") },
									modifier = Modifier
										.padding(horizontal = 16.dp)
										.fillMaxWidth(),
									keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
								)
								Row(
									modifier = Modifier
										.align(Alignment.End)
										.padding(horizontal = 16.dp)
								) {
									TextButton(
										onClick = {
											showEventDialog.value = false
										}
									) {
										Text("Dismiss")
									}
									TextButton(
										onClick = {
											filters[0] = current.value
											scope.launch {
												update()
											}
											showEventDialog.value = false
										},
										enabled = current.value.matches(Regex("^([0-9]{4,5})$"))
									) {
										Text("Confirm")
									}
								}
							}
						}
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
					Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
						list.forEach { team ->
							ElevatedCard(modifier = Modifier
								.fillMaxWidth()
								.padding(8.dp)) {
								Text(
									"${team!!["number"]} - ${team["team_name"]}",
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.padding(start = 8.dp, top = 8.dp)
								)
								val name = if ((team["robot_name"] as? String)?.isEmpty() == false) team["robot_name"] as String else null
								Text(
									name ?: "No robot name",
									style = MaterialTheme.typography.titleMedium,
									modifier = Modifier.padding(start = 8.dp)
								)
								if (team["organization"] as? String != null) {
									Text(
										team["organization"] as String,
										style = MaterialTheme.typography.titleMedium,
										modifier = Modifier.padding(start = 8.dp)
									)
								}
								Text(
									(team["program"] as Map<*, *>)["name"] as String,
									style = MaterialTheme.typography.titleSmall,
									modifier = Modifier.padding(start = 8.dp)
								)
								Text(
									team["grade"] as String,
									style = MaterialTheme.typography.titleSmall,
									modifier = Modifier.padding(start = 8.dp)
								)
								Text(
									if (team["registered"] as Boolean) "Registered" else "Not registered",
									style = MaterialTheme.typography.titleSmall,
									modifier = Modifier.padding(start = 8.dp)
								)
								Text(
									"${(team["location"] as Map<*, *>)["city"]}, ${(team["location"] as Map<*, *>)["country"]} ${(team["location"] as Map<*, *>)["postcode"]}",
									style = MaterialTheme.typography.titleSmall,
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