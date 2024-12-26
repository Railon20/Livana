package com.example.livani

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(private val productDao: ProductDao) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow(SearchQuery())
    val searchResults = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { productDao.searchProducts(_searchQuery.value.minPrice, _searchQuery.value.maxPrice, _searchQuery.value.brand, _searchQuery.value.type) }
    ).flow.cachedIn(viewModelScope)

    fun updateSearchQuery(searchQuery: SearchQuery) {
        _searchQuery.value = searchQuery
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    data class SearchQuery(
        val minPrice: Double = 0.0,
        val maxPrice: Double = Double.MAX_VALUE,
        val brand: String = "%",
        val type: String = "%"
    )

    init {
        insertSampleProducts()
    }

    private fun insertSampleProducts() {
        viewModelScope.launch {
            productDao.insertProducts(
                Product(0, "Jabón", 2.5, "Marca A", "Higiene", "url_to_soap_image.jpg"),
                Product(0, "Martillo", 15.0, "Marca B", "Ferretería", "url_to_hammer_image.jpg"),
                // Añade más productos según sea necesario
            )
        }
    }
}