#!/bin/bash
# Script to fix deprecated icons and locales
# Icons
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Filled.DirectionsRun/Icons.AutoMirrored.Filled.DirectionsRun/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Outlined.DirectionsRun/Icons.AutoMirrored.Outlined.DirectionsRun/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Filled.ReceiptLong/Icons.AutoMirrored.Filled.ReceiptLong/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Outlined.ReceiptLong/Icons.AutoMirrored.Outlined.ReceiptLong/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Filled.TrendingUp/Icons.AutoMirrored.Filled.TrendingUp/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Outlined.TrendingUp/Icons.AutoMirrored.Outlined.TrendingUp/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Filled.MenuBook/Icons.AutoMirrored.Filled.MenuBook/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Filled.OpenInNew/Icons.AutoMirrored.Filled.OpenInNew/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Filled.VolumeUp/Icons.AutoMirrored.Filled.VolumeUp/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Filled.VolumeOff/Icons.AutoMirrored.Filled.VolumeOff/g' {} +
find /app/applet/app/src/main/java/com/example -name "*.kt" -exec sed -i 's/Icons.Filled.TrendingDown/Icons.AutoMirrored.Filled.TrendingDown/g' {} +
