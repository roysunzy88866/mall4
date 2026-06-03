package space.hearagain.ridemall.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.hearagain.ridemall.data.ServiceLocator
import space.hearagain.ridemall.data.repository.StoreRepository
import space.hearagain.ridemall.model.HomeData
import space.hearagain.ridemall.model.NavCategory
import space.hearagain.ridemall.model.Product

/** 当前所在页(与内容解耦,决定导航高亮)。 */
sealed interface Route {
    data object Home : Route
    data class Category(val id: Int, val name: String) : Route
    data object Orders : Route
}

/** 右侧内容区状态。 */
sealed interface ContentState {
    data object Loading : ContentState
    data class HomeLoaded(val data: HomeData) : ContentState
    data class CategoryLoaded(val products: List<Product>) : ContentState
    data object OrdersPlaceholder : ContentState
    data object NetworkError : ContentState
}

data class StoreUiState(
    val categories: List<NavCategory> = emptyList(),
    val route: Route = Route.Home,
    val content: ContentState = ContentState.Loading,
)

/**
 * 商城状态机(D2:状态驱动单 Activity 路由)。导航/内容/网络异常都在此编排。
 * 任何接口失败 → content=NetworkError;retry() 按当前 route 重拉。
 */
class StoreViewModel(private val repo: StoreRepository) : ViewModel() {

    private val _state = MutableStateFlow(StoreUiState())
    val state: StateFlow<StoreUiState> = _state.asStateFlow()

    init {
        loadInitial()
    }

    /** 首次/重置:拉导航分类 + 推荐页;任一失败即网络异常。 */
    fun loadInitial() {
        _state.update { it.copy(route = Route.Home, content = ContentState.Loading) }
        viewModelScope.launch {
            runCatching {
                val cats = repo.categories()
                val home = repo.home()
                cats to home
            }.onSuccess { (cats, home) ->
                _state.update {
                    it.copy(categories = cats, route = Route.Home, content = ContentState.HomeLoaded(home))
                }
            }.onFailure {
                _state.update { it.copy(content = ContentState.NetworkError) }
            }
        }
    }

    fun openHome() {
        _state.update { it.copy(route = Route.Home, content = ContentState.Loading) }
        viewModelScope.launch {
            runCatching {
                val cats = _state.value.categories.ifEmpty { repo.categories() }
                cats to repo.home()
            }.onSuccess { (cats, home) ->
                _state.update {
                    it.copy(categories = cats, route = Route.Home, content = ContentState.HomeLoaded(home))
                }
            }.onFailure {
                _state.update { it.copy(content = ContentState.NetworkError) }
            }
        }
    }

    fun openCategory(category: NavCategory) {
        _state.update {
            it.copy(route = Route.Category(category.id, category.name), content = ContentState.Loading)
        }
        viewModelScope.launch {
            runCatching { repo.categoryProducts(category.id) }
                .onSuccess { products ->
                    _state.update { it.copy(content = ContentState.CategoryLoaded(products)) }
                }
                .onFailure {
                    _state.update { it.copy(content = ContentState.NetworkError) }
                }
        }
    }

    /** 订单页本刀占位(第二刀实现),不发请求。 */
    fun openOrders() {
        _state.update { it.copy(route = Route.Orders, content = ContentState.OrdersPlaceholder) }
    }

    /** 网络异常页「点击重试」:按当前 route 重新拉当前页;导航缺失则整体重载。 */
    fun retry() {
        if (_state.value.categories.isEmpty()) {
            loadInitial()
            return
        }
        when (val r = _state.value.route) {
            is Route.Home -> openHome()
            is Route.Category -> openCategory(NavCategory(r.id, r.name))
            is Route.Orders -> openOrders()
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StoreViewModel(ServiceLocator.repository) as T
    }
}
