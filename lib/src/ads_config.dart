class AdsConfig {
  final String appId;
  final String bannerId;
  final String interstitialId;
  final String nativeId;
  final String rewardedId;
  final String splashAppOpenId;
  final String resumeAppOpenId;

  /// Shimmer colors in HEX format (e.g. "#E0E0E0")
  final String? shimmerBaseColor;
  final String? shimmerHighlightColor;

  /// AdLoadingDialog appearance
  final String? dialogBackgroundColor;
  final String? dialogProgressColor;
  final String? dialogTextColor;
  final String? dialogLoadingText;
  final bool? showDialogProgressBar;

  const AdsConfig({
    this.appId = "",
    this.bannerId = "",
    this.interstitialId = "",
    this.nativeId = "",
    this.rewardedId = "",
    this.splashAppOpenId = "",
    this.resumeAppOpenId = "",
    this.shimmerBaseColor,
    this.shimmerHighlightColor,
    this.dialogBackgroundColor,
    this.dialogProgressColor,
    this.dialogTextColor,
    this.dialogLoadingText,
    this.showDialogProgressBar,
  });

  Map<String, dynamic> toMap() {
    return {
      'appId': appId,
      'bannerId': bannerId,
      'interstitialId': interstitialId,
      'nativeId': nativeId,
      'rewardedId': rewardedId,
      'splashAppOpenId': splashAppOpenId,
      'resumeAppOpenId': resumeAppOpenId,
      if (shimmerBaseColor != null) 'shimmerBaseColor': shimmerBaseColor,
      if (shimmerHighlightColor != null) 'shimmerHighlightColor': shimmerHighlightColor,
      if (dialogBackgroundColor != null) 'dialogBackgroundColor': dialogBackgroundColor,
      if (dialogProgressColor != null) 'dialogProgressColor': dialogProgressColor,
      if (dialogTextColor != null) 'dialogTextColor': dialogTextColor,
      if (dialogLoadingText != null) 'dialogLoadingText': dialogLoadingText,
      if (showDialogProgressBar != null) 'showDialogProgressBar': showDialogProgressBar,
    };
  }
}

enum BannerType {
  adaptive,
  collapsible,
  inline,
  mrec,
}

enum NativeType {
  large,
  small,
}
