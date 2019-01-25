if hash codesign 2>/dev/null; then
    codesign --force --verify --verbose --sign "Developer ID Application: Tobias Ullerich" PlayWall.app
    echo signed mac app

    zip -r PlayWall.zip PlayWall.app
else
    (>&2 echo "codesign image not found")
fi