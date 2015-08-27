<?php

require_once 'db.php';
require 'vendor/autoload.php';

use mikehaertl\wkhtmlto\Image;

\requireInput('Must provide URL of screenshot', ['url']);

\define('TYPE_URL', 0);
\define('TYPE_VIDEO', 1);

$filename = $url = \base64_decode($data['url']);
$type = TYPE_URL;

foreach ($youtubeUrls as $youtubeUrl) {
	if (strpos($filename, $youtubeUrl) === 0) {
		$type = TYPE_VIDEO;

		$trimmedUrl = \str_replace($youtubeUrl, '', $url);
		$urlParts = \explode('&', $trimmedUrl);
		foreach ($urlParts as $urlPart) {
			if (strpos($urlPart, 'v=') === 0) {
				$filename = \str_replace('v=', '', $urlPart);
				break;
			}
		}

		if($url === $filename) {
			$filename = $trimmedUrl;
		}

		break;
	}
}

if($type === TYPE_URL) {
	$file = URL_DIR . base64_encode($data['url']) .'.png';
	if(!\is_file($file)) {
		
		$image = new Image($data['url']);
		$image->setOptions(['binary' => '/usr/local/bin/wkhtmltoimage', 'width' => 1024, 'height' => 1024]);

		if(!$image->saveAs($file)) {
			\dieError('Could not get screenshot: '. $image->getError(), 'Could not get screenshot');
		}
		
		$image->send();
		exit;
	}
	//\errorLog('Can\'t get screenshot of ' . $url);

	\header('Content-type: image/png');
	\readfile($file);
} elseif ($type === TYPE_VIDEO) {
	\header('Content-type: image/jpeg');
	\readfile('http://img.youtube.com/vi/'. $filename .'/0.jpg');
}

// https://img.youtube.com/vi/cxLG2wtE7TM/0.jpg