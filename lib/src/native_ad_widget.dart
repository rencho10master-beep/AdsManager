import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'ads_config.dart';

/// Flutter Widget to display Native Ads without writing any XML.
/// Automatically renders our Shimmer effect and binds AdMob Native Ad.
class NativeAdWidget extends StatelessWidget {
  final NativeType type;
  final double? height;

  const NativeAdWidget({
    Key? key,
    this.type = NativeType.large,
    this.height,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if (defaultTargetPlatform != TargetPlatform.android) {
      return const SizedBox.shrink();
    }

    final double widgetHeight = height ?? (type == NativeType.small ? 140.0 : 330.0);

    return SizedBox(
      height: widgetHeight,
      width: double.infinity,
      child: AndroidView(
        viewType: 'com.ads.manager/native',
        creationParams: {
          'type': type.index, // 0 = Large, 1 = Small
        },
        creationParamsCodec: const StandardMessageCodec(),
      ),
    );
  }
}
