import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'ads_config.dart';

/// Flutter Widget to display Banner Ads without writing any XML.
/// Supports Adaptive, Collapsible, Inline, and MREC (300x250).
class BannerAdWidget extends StatelessWidget {
  final BannerType type;
  final String collapsiblePosition; // "bottom" or "top"
  final int maxHeightDp;
  final double? height;

  const BannerAdWidget({
    Key? key,
    this.type = BannerType.adaptive,
    this.collapsiblePosition = "bottom",
    this.maxHeightDp = 100,
    this.height,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform != TargetPlatform.android) {
      return const SizedBox.shrink();
    }

    final double widgetHeight = height ?? _getDefaultHeight(type);

    return SizedBox(
      height: widgetHeight,
      width: double.infinity,
      child: AndroidView(
        viewType: 'com.ads.manager/banner',
        creationParams: {
          'type': type.index,
          'collapsiblePosition': collapsiblePosition,
          'maxHeightDp': maxHeightDp,
        },
        creationParamsCodec: const StandardMessageCodec(),
      ),
    );
  }

  double _getDefaultHeight(BannerType type) {
    switch (type) {
      case BannerType.mrec:
        return 250.0;
      case BannerType.inline:
        return maxHeightDp.toDouble();
      case BannerType.collapsible:
      case BannerType.adaptive:
      default:
        return 60.0;
    }
  }
}
