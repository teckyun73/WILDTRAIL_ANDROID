package com.wildtrail.app.feature.trips

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.wildtrail.app.BuildConfig
import com.wildtrail.app.data.dto.TripPlanResponseDto
import com.wildtrail.app.ui.theme.Forest

internal data class RouteStopMarkerData(
    val routeIndex: Int,
    val stop: RouteStop,
    val latitude: Double,
    val longitude: Double,
)

private data class MappedRouteStop(
    val routeIndex: Int,
    val stop: RouteStop,
    val latLng: LatLng,
)

@Composable
fun TripNativeMapScreen(
    plan: TripPlanResponseDto,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val stops = remember(plan) { tripRouteStops(plan) }
    val coordinateStops = remember(stops) { stops.filter { it.hasCoordinates } }
    val summary = remember(stops) { routeSummary(stops) }
    var selectedStop by remember(stops) { mutableStateOf<RouteStop?>(coordinateStops.lastOrNull() ?: stops.lastOrNull()) }
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var locationRequestCount by remember { mutableStateOf(0) }
    var locationPermissionMessage by remember { mutableStateOf<String?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionMessage = if (hasLocationPermission) {
            "현재 위치 표시가 활성화되었습니다."
        } else {
            "위치 권한을 허용하면 현재 위치를 지도에 함께 표시할 수 있습니다. Android 설정에서 위치 권한을 허용한 뒤 다시 시도해 주세요."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("여행 지도", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "${plan.region} · ${plan.hotspotName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RouteSummaryRow(summary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                modifier = Modifier.weight(1f),
            ) {
                Text("계획으로")
            }
            Button(
                onClick = { openRouteInMapApp(context, stops) },
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                modifier = Modifier.weight(1f),
            ) {
                Text("경로 안내")
            }
        }
        if (coordinateStops.isEmpty()) {
            EmptyMapPanel()
        } else {
            Surface(
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (BuildConfig.HAS_MAPS_API_KEY) {
                        NativeRouteMap(
                            stops = stops,
                            hasLocationPermission = hasLocationPermission,
                            locationRequestCount = locationRequestCount,
                            selectedStop = selectedStop,
                            onStopSelected = { selectedStop = it },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        MissingMapsKeyPanel(modifier = Modifier.fillMaxSize())
                    }
                    Button(
                        onClick = {
                            if (context.hasLocationPermission()) {
                                hasLocationPermission = true
                                locationPermissionMessage = "현재 위치로 지도를 이동합니다."
                                locationRequestCount += 1
                            } else {
                                locationPermissionMessage = "위치 권한 요청을 확인해 주세요."
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                    ) {
                        Text("내 위치")
                    }
                    locationPermissionMessage?.let { message ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                        ) {
                            Text(
                                message,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                }
            }
        }
        selectedStop?.let { stop ->
            RouteStopDetailPanel(
                stop = stop,
                onOpenPlace = { openPlaceInMapApp(context, stop) },
                onClose = { selectedStop = null },
            )
        }
        Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("동선 지점", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${summary.coordinateStopCount}/${summary.stopCount}개 지점에 좌표가 있으며 ${formatRouteDistance(summary.straightLineDistanceKm)}입니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                stops.forEachIndexed { index, stop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedStop = stop }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(routeStopColor(stop).copy(alpha = if (stop.hasCoordinates) 1f else 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${index + 1}",
                                color = if (stop.hasCoordinates) Color.White else Forest,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stop.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                stop.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RouteSummaryRow(summary: RouteSummary) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RouteSummaryChip("지점 ${summary.stopCount}곳")
        RouteSummaryChip("좌표 ${summary.coordinateStopCount}곳")
        RouteSummaryChip(formatRouteDistance(summary.straightLineDistanceKm))
    }
}

@Composable
private fun RouteSummaryChip(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MissingMapsKeyPanel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Forest.copy(alpha = 0.08f))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Google Maps API 키를 설정하면 지도 타일과 마커가 표시됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
@Composable
private fun EmptyMapPanel() {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Text(
            "지도에 표시할 좌표가 없습니다.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RouteStopDetailPanel(
    stop: RouteStop,
    onOpenPlace: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 1.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(routeStopColor(stop)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stop.role.firstOrNull()?.toString() ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stop.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                if (stop.hasCoordinates) {
                    "좌표 ${"%.5f".format(stop.latitude)}, ${"%.5f".format(stop.longitude)}"
                } else {
                    "좌표가 없는 지점입니다. 장소 검색으로 위치를 확인할 수 있습니다."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onOpenPlace,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("장소 검색")
                }
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("닫기", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun NativeRouteMap(
    stops: List<RouteStop>,
    hasLocationPermission: Boolean,
    locationRequestCount: Int,
    selectedStop: RouteStop?,
    onStopSelected: (RouteStop) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            onCreate(null)
        }
    }

    DisposableEffect(lifecycle, mapView) {
        var isDestroyed = false
        fun destroyMapView() {
            if (!isDestroyed) {
                isDestroyed = true
                mapView.onDestroy()
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> destroyMapView()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            destroyMapView()
        }
    }

    LaunchedEffect(stops, hasLocationPermission, selectedStop) {
        mapView.getMapAsync { googleMap ->
            renderRouteMap(
                context = context,
                googleMap = googleMap,
                stops = stops,
                hasLocationPermission = hasLocationPermission,
                selectedStop = selectedStop,
                onStopSelected = onStopSelected,
            )
        }
    }

    LaunchedEffect(locationRequestCount) {
        if (locationRequestCount > 0) {
            mapView.getMapAsync { googleMap ->
                moveToLastKnownLocation(context, googleMap)
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

@SuppressLint("MissingPermission")
private fun renderRouteMap(
    context: Context,
    googleMap: GoogleMap,
    stops: List<RouteStop>,
    hasLocationPermission: Boolean,
    selectedStop: RouteStop?,
    onStopSelected: (RouteStop) -> Unit,
) {
    val mappedStops = routeStopMarkerData(stops).map { markerData ->
        MappedRouteStop(
            routeIndex = markerData.routeIndex,
            stop = markerData.stop,
            latLng = LatLng(markerData.latitude, markerData.longitude),
        )
    }
    googleMap.clear()
    googleMap.uiSettings.isZoomControlsEnabled = true
    googleMap.uiSettings.isMapToolbarEnabled = true
    googleMap.uiSettings.isMyLocationButtonEnabled = hasLocationPermission
    if (hasLocationPermission && context.hasLocationPermission()) {
        googleMap.isMyLocationEnabled = true
    }
    val markerStops = mutableMapOf<Marker, RouteStop>()
    mappedStops.forEach { mappedStop ->
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(mappedStop.latLng)
                .title("${mappedStop.routeIndex + 1}. ${mappedStop.stop.name}")
                .snippet(mappedStop.stop.role)
                .icon(BitmapDescriptorFactory.defaultMarker(routeStopHue(mappedStop.stop)))
        )
        if (marker != null) {
            markerStops[marker] = mappedStop.stop
            if (mappedStop.stop == selectedStop) {
                marker.showInfoWindow()
            }
        }
    }
    googleMap.setOnMarkerClickListener { marker ->
        markerStops[marker]?.let(onStopSelected)
        marker.showInfoWindow()
        true
    }
    if (mappedStops.size > 1) {
        googleMap.addPolyline(
            PolylineOptions()
                .addAll(mappedStops.map { it.latLng })
                .color(0xFF215B43.toInt())
                .width(8f)
        )
        val boundsBuilder = LatLngBounds.Builder()
        mappedStops.forEach { boundsBuilder.include(it.latLng) }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80))
    } else {
        mappedStops.firstOrNull()?.latLng?.let { latLng ->
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
        }
    }
}

private fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
private fun moveToLastKnownLocation(context: Context, googleMap: GoogleMap) {
    if (!context.hasLocationPermission()) return
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
    val location = locationManager.getProviders(true)
        .mapNotNull { provider ->
            try {
                locationManager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            }
        }
        .maxByOrNull(Location::getTime)
    if (location != null) {
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                15f,
            )
        )
    } else {
        Toast.makeText(context, "최근 위치를 아직 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
    }
}

internal fun routeStopMarkerData(stops: List<RouteStop>): List<RouteStopMarkerData> {
    return stops.mapIndexedNotNull { index, stop ->
        val latitude = stop.latitude
        val longitude = stop.longitude
        if (latitude != null && longitude != null) {
            RouteStopMarkerData(index, stop, latitude, longitude)
        } else {
            null
        }
    }
}

internal fun routeStopColor(stop: RouteStop): Color {
    return when {
        stop.role.contains("출발") -> Color(0xFF2563EB)
        stop.role.contains("경유") -> Color(0xFFF59E0B)
        stop.role.contains("관찰") -> Forest
        else -> Color(0xFF6B7280)
    }
}

internal fun routeStopHue(stop: RouteStop): Float {
    return when {
        stop.role.contains("출발") -> BitmapDescriptorFactory.HUE_AZURE
        stop.role.contains("경유") -> BitmapDescriptorFactory.HUE_ORANGE
        stop.role.contains("관찰") -> BitmapDescriptorFactory.HUE_GREEN
        else -> BitmapDescriptorFactory.HUE_RED
    }
}

