package com.example.livani

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.compose.navigation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.paging.LoadState
import androidx.paging.compose.items

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "buscar"
    ) {
        composable(
            "buscar",
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700))
            }
        ) {
        SearchScreen()
        }
        composable("inicio") { HomeScreen() }
        composable("carrito") { CartScreen() }
        composable("pedidos") { OrdersScreen() }
        composable("historial") { HistoryScreen() }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("inicio", "Inicio", Icons.Default.Home),
        BottomNavItem("carrito", "Carrito", Icons.Default.ShoppingCart),
        BottomNavItem("buscar", "Buscar", Icons.Default.Search),
        BottomNavItem("pedidos", "Pedidos", Icons.Default.List),
        BottomNavItem("historial", "Historial", Icons.Default.Done)
    )
    NavigationBar {
        val currentRoute = currentRoute(navController)
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.route) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination so we don't have to press back multiple times
                        popUpTo(navController.graph.startDestinationId)
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

// Aquí definimos cada pantalla, por ejemplo:

@Composable
fun HomeScreen() {
    Text("Pantalla de Inicio")
}

@Composable
fun CartScreen() {
    Text("Pantalla de Carrito")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val viewModel: ProductViewModel = hiltViewModel()
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }

    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = minPrice,
            onValueChange = { minPrice = it },
            label = { Text("Precio mínimo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = maxPrice,
            onValueChange = { maxPrice = it },
            label = { Text("Precio máximo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Marca") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        val productTypes = listOf("Limpieza", "Higiene", "Ferretería", "Alimentos", "Bebidas")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = type,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Producto") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                productTypes.forEach { selectionOption ->
                    DropdownMenuItem(
                        onClick = {
                            type = selectionOption
                            expanded = false
                        },
                        text = { Text(text = selectionOption) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            try {
                val min = if (minPrice.isNotBlank()) minPrice.toDouble() else 0.0
                val max = if (maxPrice.isNotBlank()) maxPrice.toDouble() else Double.MAX_VALUE
                viewModel.updateSearchQuery(ProductViewModel.SearchQuery(min, max, brand, type))
            } catch (e: NumberFormatException) {
                viewModel.setErrorMessage("Por favor, ingrese números válidos para el rango de precios.")
            }
        }) {
            Text("Buscar")
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        when {
            searchResults.loadState.refresh is LoadState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            searchResults.itemCount == 0 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron productos que coincidan con tu búsqueda.")
                }
            }
            else -> {
                LazyColumn {
                    items(searchResults) { product ->
                        product?.let { ProductItem(it) }
                    }
                    }
                    when {
                        searchResults.loadState.append is LoadState.Loading -> {
                            item {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                        searchResults.loadState.append is LoadState.Error -> {
                            item {
                                Text("Error al cargar más productos", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product?) {
    product?.let { nonNullProduct ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del producto
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(nonNullProduct.imageUrl)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .error(R.drawable.pausa6)
                        .build(),
                    contentDescription = "Imagen del producto ${nonNullProduct.name}",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.pausa6)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Detalles del producto
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = nonNullProduct.name, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Precio: $${nonNullProduct.price}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Marca: ${nonNullProduct.brand}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "Tipo: ${nonNullProduct.type}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    } ?: run {
        // Aquí podrías manejar el caso donde el producto es nulo, por ejemplo, mostrando un mensaje
        Text("Producto no disponible")
    }
}
@Composable
fun OrdersScreen() {
    Text("Pantalla de Pedidos Pendientes")
}

@Composable
fun HistoryScreen() {
    Text("Pantalla de Historial")
}

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)