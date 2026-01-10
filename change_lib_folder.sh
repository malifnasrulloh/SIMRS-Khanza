lib_folder="C:/SIMRS-Khanza-fork/lib/"

find . -type f -name "project.properties" -exec sed -i "s|/Users/windiartonugroho/Lib/|$lib_folder|g" {} +
find . -type f -name "project.properties" -exec sed -i "s|/Users/windiartonugroho/lib/|$lib_folder|g" {} +
find . -type f -name "project.properties" -exec sed -i "s|/Users/windiartonugroho/Documents/lib/|$lib_folder|g" {} +
find . -type f -name "project.properties" -exec sed -i "s|../../../../lib/|$lib_folder|g" {} +


dist_folder="C:/KhanzaHMSWindowsAlip/"

find . -type f -name "project.properties" -exec sed -i "s|dist.dir=dist|dist.dir=$dist_folder|g" {} +



find . -name "project.properties" -exec sed -i '/file.reference.commons-codec-1.12.jar/d' {} +

