import 'package:flutter/material.dart';

void main() => runApp(const ZaffranTaxiApp());

const saffron = Color(0xFFD9A321);
const ink = Color(0xFF18212F);
const canvas = Color(0xFFF7F6F2);

class ZaffranTaxiApp extends StatelessWidget {
  const ZaffranTaxiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'تاکسی زعفران',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: saffron),
        scaffoldBackgroundColor: canvas,
        fontFamily: 'Roboto',
      ),
      home: const RoleSelectionPage(),
    );
  }
}

class PassengerHomePage extends StatefulWidget {
  const PassengerHomePage({super.key});

  @override
  State<PassengerHomePage> createState() => _PassengerHomePageState();
}

class _PassengerHomePageState extends State<PassengerHomePage> {
  final originController = TextEditingController();
  final destinationController = TextEditingController();
  String selectedService = 'تاکسی';
  String tripMode = 'یک‌طرفه';

  final services = const [
    ('تاکسی', Icons.local_taxi_rounded),
    ('بانوان', Icons.woman_rounded),
    ('اشتراکی', Icons.groups_rounded),
    ('بین‌شهری', Icons.route_rounded),
    ('پیک', Icons.delivery_dining_rounded),
    ('وانت', Icons.local_shipping_rounded),
    ('یدک‌کش', Icons.car_repair_rounded),
  ];

  @override
  void dispose() {
    originController.dispose();
    destinationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: ink,
          foregroundColor: Colors.white,
          title: const Row(
            children: [
              Icon(Icons.local_taxi_rounded, color: saffron, size: 27),
              SizedBox(width: 9),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('تاکسی زعفران', style: TextStyle(fontWeight: FontWeight.w800)),
                  Text('از روستای تو تا مقصد تو',
                      style: TextStyle(fontSize: 11, color: Colors.white70)),
                ],
              ),
            ],
          ),
          actions: [
            IconButton(
              tooltip: 'پشتیبانی هوشمند',
              onPressed: () => _showInfo('دستیار هوشمند زعفران',
                  'اتصال دستیار فارسی پس از آماده‌شدن سرور داخلی فعال می‌شود.'),
              icon: const Icon(Icons.smart_toy_outlined),
            ),
          ],
        ),
        body: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Container(
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color: ink,
                borderRadius: BorderRadius.circular(22),
              ),
              child: const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('سفر بعدی‌ات از کجاست؟',
                      style: TextStyle(color: Colors.white, fontSize: 21, fontWeight: FontWeight.bold)),
                  SizedBox(height: 7),
                  Text('از شهر تا روستا؛ از روستا تا شهر',
                      style: TextStyle(color: Colors.white70)),
                ],
              ),
            ),
            const SizedBox(height: 18),
            const Text('نوع سرویس', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 10),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: services.map((service) {
                final selected = selectedService == service.$1;
                return ChoiceChip(
                  selected: selected,
                  avatar: Icon(service.$2, size: 18,
                      color: selected ? ink : Colors.blueGrey),
                  label: Text(service.$1),
                  selectedColor: saffron,
                  onSelected: (_) => setState(() => selectedService = service.$1),
                );
              }).toList(),
            ),
            const SizedBox(height: 20),
            const Text('مسیر سفر', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 10),
            _addressField(originController, 'مبدأ؛ نام روستا، شهر یا نشانی', Icons.my_location),
            const SizedBox(height: 10),
            _addressField(destinationController, 'مقصد؛ نام روستا، شهر یا نشانی', Icons.location_on_outlined),
            const SizedBox(height: 14),
            SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'یک‌طرفه', label: Text('یک‌طرفه'), icon: Icon(Icons.arrow_forward)),
                ButtonSegment(value: 'رفت‌وبرگشت', label: Text('رفت‌وبرگشت'), icon: Icon(Icons.swap_horiz)),
              ],
              selected: {tripMode},
              onSelectionChanged: (value) => setState(() => tripMode = value.first),
            ),
            const SizedBox(height: 18),
            FilledButton.icon(
              style: FilledButton.styleFrom(
                backgroundColor: ink,
                minimumSize: const Size.fromHeight(54),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
              ),
              onPressed: _requestRide,
              icon: const Icon(Icons.search_rounded),
              label: const Text('جست‌وجوی سرویس', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            ),
            const SizedBox(height: 16),
            Card(
              elevation: 0,
              color: const Color(0xFFFFF3D2),
              child: const Padding(
                padding: EdgeInsets.all(13),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Icon(Icons.info_outline, color: ink),
                    SizedBox(width: 9),
                    Expanded(
                      child: Text(
                        'نسخه آغازین رابط کاربری است. اعزام راننده، قیمت واقعی، پرداخت و ردیابی پس از اتصال و آزمایش سرور فعال می‌شوند.',
                        style: TextStyle(color: ink, height: 1.5),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
        bottomNavigationBar: NavigationBar(
          selectedIndex: 0,
          destinations: const [
            NavigationDestination(icon: Icon(Icons.home_outlined), label: 'خانه'),
            NavigationDestination(icon: Icon(Icons.receipt_long_outlined), label: 'سفرها'),
            NavigationDestination(icon: Icon(Icons.person_outline), label: 'حساب من'),
          ],
          onDestinationSelected: (index) {
            if (index != 0) {
              _showInfo('به‌زودی', 'این بخش در مرحله بعدی توسعه متصل می‌شود.');
            }
          },
        ),
      ),
    );
  }

  Widget _addressField(TextEditingController controller, String label, IconData icon) {
    return TextField(
      controller: controller,
      textInputAction: TextInputAction.next,
      decoration: InputDecoration(
        prefixIcon: Icon(icon, color: saffron),
        labelText: label,
        filled: true,
        fillColor: Colors.white,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(14), borderSide: BorderSide.none),
      ),
    );
  }

  void _requestRide() {
    if (originController.text.trim().isEmpty || destinationController.text.trim().isEmpty) {
      _showInfo('تکمیل مسیر', 'لطفاً مبدأ و مقصد را وارد کنید.');
      return;
    }
    _showInfo(
      'درخواست ثبت آزمایشی',
      'سرویس: $selectedService\nنوع سفر: $tripMode\nمبدأ: ${originController.text}\nمقصد: ${destinationController.text}\n\nبرای ارسال درخواست واقعی، اتصال سرور هنوز لازم است.',
    );
  }

  void _showInfo(String title, String message) {
    showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Text(message),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('متوجه شدم')),
        ],
      ),
    );
  }
}


class RoleSelectionPage extends StatefulWidget {
  const RoleSelectionPage({super.key});

  @override
  State<RoleSelectionPage> createState() => _RoleSelectionPageState();
}

class _RoleSelectionPageState extends State<RoleSelectionPage> {
  String role = 'مسافر';

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        backgroundColor: ink,
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(22),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Spacer(),
                const Icon(Icons.local_taxi_rounded, color: saffron, size: 76),
                const SizedBox(height: 18),
                const Text('تاکسی زعفران',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.white, fontSize: 29, fontWeight: FontWeight.w900)),
                const SizedBox(height: 8),
                const Text('از روستای تو تا مقصد تو',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.white70, fontSize: 15)),
                const SizedBox(height: 42),
                const Text('برای ادامه، انتخاب کن:',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold)),
                const SizedBox(height: 18),
                _roleCard('مسافر', Icons.person_rounded, 'درخواست سفر و پیگیری مسیر'),
                const SizedBox(height: 12),
                _roleCard('راننده', Icons.local_taxi_rounded, 'ثبت‌نام، مدارک و دریافت سفر'),
                const Spacer(),
                FilledButton(
                  style: FilledButton.styleFrom(
                    backgroundColor: saffron,
                    foregroundColor: ink,
                    minimumSize: const Size.fromHeight(56),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                  onPressed: () => Navigator.push(context, MaterialPageRoute(
                    builder: (_) => role == 'مسافر'
                        ? const PassengerHomePage()
                        : const DriverHomePage(),
                  )),
                  child: const Text('ادامه', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w900)),
                ),
                const SizedBox(height: 16),
                const Text('پشتیبانی هوشمند فارسی زعفران',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.white60, fontSize: 12)),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _roleCard(String value, IconData icon, String subtitle) {
    final selected = role == value;
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: () => setState(() => role = value),
      child: Container(
        padding: const EdgeInsets.all(17),
        decoration: BoxDecoration(
          color: selected ? saffron : const Color(0xFF263244),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: selected ? Colors.white : Colors.white24, width: selected ? 2 : 1),
        ),
        child: Row(
          children: [
            Icon(icon, color: selected ? ink : Colors.white, size: 32),
            const SizedBox(width: 14),
            Expanded(child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(value, style: TextStyle(color: selected ? ink : Colors.white, fontSize: 19, fontWeight: FontWeight.bold)),
                const SizedBox(height: 3),
                Text(subtitle, style: TextStyle(color: selected ? ink.withValues(alpha: 0.75) : Colors.white70, fontSize: 12)),
              ],
            )),
            Icon(selected ? Icons.radio_button_checked : Icons.radio_button_off,
                color: selected ? ink : Colors.white70),
          ],
        ),
      ),
    );
  }
}

class DriverHomePage extends StatelessWidget {
  const DriverHomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: ink,
          foregroundColor: Colors.white,
          title: const Text('پنل راننده'),
        ),
        body: ListView(
          padding: const EdgeInsets.all(18),
          children: [
            Container(
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(color: ink, borderRadius: BorderRadius.circular(18)),
              child: const Row(children: [
                Icon(Icons.verified_user_outlined, color: saffron, size: 34),
                SizedBox(width: 12),
                Expanded(child: Text('ثبت‌نام و احراز هویت راننده',
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold))),
              ]),
            ),
            const SizedBox(height: 16),
            const _DriverStep(icon: Icons.person_outline, title: 'اطلاعات هویتی', detail: 'نام، شماره همراه و مشخصات فردی'),
            const _DriverStep(icon: Icons.badge_outlined, title: 'مدارک رانندگی', detail: 'گواهینامه و مدارک لازم'),
            const _DriverStep(icon: Icons.directions_car_outlined, title: 'مشخصات خودرو', detail: 'نوع خودرو، پلاک و مدارک خودرو'),
            const _DriverStep(icon: Icons.account_balance_outlined, title: 'اطلاعات تسویه', detail: 'شماره شبا به نام راننده'),
            const SizedBox(height: 14),
            const Card(
              child: Padding(
                padding: EdgeInsets.all(14),
                child: Text('هوش مصنوعی مدارک را از نظر خوانایی، کامل‌بودن و مغایرت بررسی می‌کند؛ تأیید نهایی هویت و فعال‌سازی راننده با بررسی ناظر انجام می‌شود. اطلاعات بانکی و انتقال وجه نیز فقط از مسیر امن سرور و پس از تأیید مجاز انجام خواهد شد.',
                    style: TextStyle(height: 1.6)),
              ),
            ),
            const SizedBox(height: 12),
            FilledButton(
              style: FilledButton.styleFrom(backgroundColor: ink, minimumSize: const Size.fromHeight(52)),
              onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('فرم ثبت‌نام و اتصال امن سرور در مرحله بعدی تکمیل می‌شود.')),
              ),
              child: const Text('شروع ثبت‌نام راننده'),
            ),
          ],
        ),
      ),
    );
  }
}

class _DriverStep extends StatelessWidget {
  const _DriverStep({required this.icon, required this.title, required this.detail});
  final IconData icon;
  final String title;
  final String detail;

  @override
  Widget build(BuildContext context) => Card(
    elevation: 0,
    child: ListTile(
      leading: Icon(icon, color: saffron, size: 29),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
      subtitle: Text(detail),
      trailing: const Icon(Icons.chevron_left),
    ),
  );
}
