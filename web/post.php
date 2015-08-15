<?php

require_once 'db.php';

\requireInput('Must provide user credentials', ['globalUserId']);
\requireInput('Must provide item details', ['itemId', 'itemType', 'itemDateTime']);

$globalUserId = \intval($data['globalUserId']);
$itemId = \intval($data['itemId']);
$itemType = \intval($data['itemType']);
$itemData = null;
$itemDateTime = \intval($data['itemDateTime']);

// If the item type is an image, save the image, otherwise grab the text
if ($itemType == 0 && isset($_FILES['itemData'])) {
	$check = \getimagesize($_FILES['itemData']['tmp_name']);
    if($check !== false) {
    	$target_file = $globalUserId . '-' . $itemId . '-' . \basename($_FILES['itemData']['name']);

    	if (move_uploaded_file($_FILES['itemData']['tmp_name'], IMG_DIR . $target_file)) {
    		$itemData = $target_file;
		} else {
			\dieError('Could not move uploaded file to target directory', 'Error Saving Image');
		}
    } else {
		\dieError('Could not get dimensions of image, presume other file type', 'File is not an image');
    }
} else if (!isset($data['itemData'])) {
	\dieError('No text-based item data provided', 'Must provide item data');
} else {
	$itemData = SQLite3::escapeString($data['itemData']);
}

// Save to database
if ($stmt = $db->prepare('INSERT INTO `item` (`globalItemId`, `localItemId`, `globalUserId`, `itemType`, `itemData`, `itemDateTime`) VALUES (:globalItemId, :localItemId, :globalUserId, :itemType, :itemData, :itemDateTime);')) {
	$stmt->bindValue(':globalItemId', $globalUserId . ':' . $itemId, SQLITE3_TEXT);
	$stmt->bindValue(':localItemId', $itemId, SQLITE3_INTEGER);
	$stmt->bindValue(':globalUserId', $globalUserId, SQLITE3_INTEGER);
	$stmt->bindValue(':itemType', $itemType, SQLITE3_INTEGER);
	$stmt->bindValue(':itemData', $itemData, SQLITE3_TEXT);
	$stmt->bindValue(':itemDateTime', $itemDateTime, SQLITE3_INTEGER);

	if(!$stmt->execute()) {
		\dieError($db->lastErrorMsg(), 'Could not insert into DB');
	}

	$stmt->close();

	\dieResult(['success' => 'A-OK', 'itemId' => $globalUserId . ':' . $itemId]);
} else {
	\dieError($db->lastErrorMsg(), 'Internal Server Error');
}
