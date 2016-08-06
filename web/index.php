<html>
<head><title>Reset Database</title>
</head>
<body style="margin: 0;">
<div style="font-family: sans-serif; font-weight: bold; font-size: smaller; width: 100%; text-align: center; margin-top: 2em;">
<?php

/*	require_once 'db.php';

	if (!$db->exec('UPDATE `item` SET `itemDeleted`=1')) {
		\dieError($db->lastErrorMsg(), 'Could not delete existing items');
	}

	if ($stmt = $db->prepare('UPDATE `item` SET `itemDeleted`=0, itemDateTime=:itemDateTime WHERE `localItemId`=0')) {
		$itemDateTime = (new DateTime('next week'))->getTimestamp();
		$stmt->bindParam(':itemDateTime', $itemDateTime, SQLITE3_INTEGER);

		if(!$stmt->execute()) {
			\dieError($db->lastErrorMsg(), 'Could not reset welcome messaages');
		}
		$stmt->close();
	} else {
		\dieError($db->lastErrorMsg(), 'Could not prepare to reset welcome messaages');
	}
*/
if(isset($_POST['reset'])) {
	if(rename('cocurator.db', 'cocurator-'. date('U') .'.db') && copy('init-demo.db', 'cocurator.db')) {
		print 'Reset Successful!';
	} else {
		print 'Reset Failed!';
	}
} elseif(isset($_POST['shutdown'])) {
	print 'Shutdown initated! Please wait 2 minutes.';
	file_put_contents('.shutdown', ' ');
}
?>
</div>
<form action="<?php print $_SERVER['PHP_SELF']; ?>" method="post">
<button id="reset" name="reset" style="width: 50%; height: 50%; margin: 15% 25% 0 25%;">
Reset Co-Curator Database
</button>
</form>
<form action="<?php print $_SERVER['PHP_SELF']; ?>" method="post">
<button id="shutdown" name="shutdown" style="width: 50%; height: 10%; margin: 10px 25% 0 25%;">
Shutdown
</button>
</form>
</body>
</html>
