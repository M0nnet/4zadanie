package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

// Data Model
data class Place(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val imageResId: Int,
    val rating: Float = 0f
)

// Fake Data Repository
object PlacesRepository {
    private val places = listOf(
        Place(1, "Башня Белен", "Достопримечательности", "Башня Торре-де-Белен — укреплённое сооружение на острове в реке Тежу в одноимённом районе Лиссабона. Построена в 1515—1521 годах Франсишку де Аррудой в честь открытия Васко да Гама морского пути в Индию и служила поочерёдно небольшой оборонительной крепостью, пороховым складом, тюрьмой и таможней.", R.drawable.tower, 4.8f),
        Place(2, "Парк Эдуарда VII", "Парки", "Парк Эдуарда VII - общественный парк в Лиссабоне, Португалия. Парк занимает площадь в 26 гектаров (64 акра) к северу от Авениды да Либердаде и площади Маркиза Помбала в центре Лиссабона. Парк назван в честь короля Великобритании Эдуарда VII, который посетил Португалию в 1903 году, чтобы укрепить отношения между двумя странами и подтвердить англо-португальский союз. Лиссабонская книжная ярмарка ежегодно проводится в парке Эдуарду VII.", R.drawable.eduardo_park, 4.5f),
        Place(3, "Ресторан Ramiro", "Рестораны", "Прекрасное место, чтобы отведать вкуснейшие и свежайшие морепродукты. Очень популярное место, время ожидания на входе может занять до часа", R.drawable.ramiro_restaurants, 4.7f)
    )

    fun getCategories(): List<String> = listOf("Достопримечательности", "Парки", "Рестораны")

    fun getPlacesByCategory(category: String): List<Place> = places.filter { it.category == category }

    fun getPlaceById(id: Int): Place? = places.find { it.id == id }
}

// ViewModel
class PlacesViewModel : ViewModel() {
    private val _places = MutableStateFlow<List<Place>>(emptyList())
    val places: StateFlow<List<Place>> = _places

    fun loadPlaces(category: String) {
        _places.value = PlacesRepository.getPlacesByCategory(category)
    }
}

// Navigation
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "categories") {
        composable("categories") { CategoriesScreen(navController) }
        composable("places/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            PlacesScreen(category, navController)
        }
        composable("details/{placeId}") { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")?.toIntOrNull() ?: -1
            DetailsScreen(placeId)
        }
    }
}

// UI Components
@Composable
fun CategoriesScreen(navController: NavController) {
    val categories = PlacesRepository.getCategories()
    LazyColumn(modifier = Modifier.padding(top = 32.dp)) {
        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { navController.navigate("places/$category") }
            ) {
                Text(category, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun PlacesScreen(category: String, navController: NavController) {
    val viewModel: PlacesViewModel = viewModel()
    val places by viewModel.places.collectAsState()

    LaunchedEffect(category) {
        viewModel.loadPlaces(category)
    }

    LazyColumn {
        items(places) { place ->
            PlaceItem(place) { navController.navigate("details/${place.id}") }
        }
    }
}

@Composable
fun PlaceItem(place: Place, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = place.imageResId),
                contentDescription = place.name,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = place.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Рейтинг: ${place.rating}")
            }
        }
    }
}

@Composable
fun DetailsScreen(placeId: Int) {
    Log.d("DetailsScreen", "Received placeId: $placeId")

    val place = PlacesRepository.getPlaceById(placeId)

    if (place == null) {
        Text("Место не найдено", modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = place.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(id = place.imageResId),
            contentDescription = place.name,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = place.description)
    }
}
