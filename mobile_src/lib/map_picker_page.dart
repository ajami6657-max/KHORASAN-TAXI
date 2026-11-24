import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

class MapPickerPage extends StatefulWidget {
  const MapPickerPage({super.key, required this.title, this.initialPoint});
  final String title;
  final String? initialPoint;

  @override
  State<MapPickerPage> createState() => _MapPickerPageState();
}

class _MapPickerPageState extends State<MapPickerPage> {
  static const LatLng birjand = LatLng(32.8597, 59.2211);
  LatLng? selected;

  @override
  void initState() {
    super.initState();
    final parts = (widget.initialPoint ?? '').split(',');
    if (parts.length == 2) {
      final lat = double.tryParse(parts[0].trim());
      final lon = double.tryParse(parts[1].trim());
      if (lat != null && lon != null) selected = LatLng(lat, lon);
    }
  }

  @override
  Widget build(BuildContext context) {
    final center = selected ?? birjand;
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        appBar: AppBar(
          title: Text(widget.title),
          backgroundColor: const Color(0xFF18212F),
          foregroundColor: Colors.white,
        ),
        body: Stack(
          children: [
            FlutterMap(
              options: MapOptions(
                initialCenter: center,
                initialZoom: 13,
                onTap: (_, point) => setState(() => selected = point),
              ),
              children: [
                TileLayer(
                  urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                  userAgentPackageName: 'ir.zaffran.taxi.zaffran_taxi',
                ),
                if (selected != null)
                  MarkerLayer(markers: [
                    Marker(
                      point: selected!,
                      width: 52,
                      height: 52,
                      child: const Icon(Icons.location_pin, color: Color(0xFFD9A321), size: 48),
                    ),
                  ]),
                RichAttributionWidget(
                  attributions: [TextSourceAttribution('© OpenStreetMap contributors')],
                ),
              ],
            ),
            Positioned(
              left: 16, right: 16, bottom: 18,
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(selected == null
                        ? 'برای تعیین ${widget.title} روی نقشه لمس کنید.'
                        : 'موقعیت انتخاب‌شده: ${selected!.latitude.toStringAsFixed(6)}, ${selected!.longitude.toStringAsFixed(6)}'),
                      const SizedBox(height: 10),
                      SizedBox(
                        width: double.infinity,
                        child: FilledButton(
                          onPressed: selected == null ? null : () => Navigator.pop(
                            context,
                            '${selected!.latitude.toStringAsFixed(6)}, ${selected!.longitude.toStringAsFixed(6)}',
                          ),
                          child: const Text('تأیید موقعیت'),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
