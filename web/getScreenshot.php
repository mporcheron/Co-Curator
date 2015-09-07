<?php

require_once 'db.php';
require 'vendor/autoload.php';

use mikehaertl\wkhtmlto\Image;

\requireInput('Must provide URL of screenshot', ['url']);

\define('TYPE_URL', 0);
\define('TYPE_VIDEO', 1);

$filename = $url = \base64_decode($data['url']);
$type = TYPE_URL;


$file = URL_DIR . $data['url'] .'.png';
if(\is_file($file)) {
	\header('Content-Type: image/png');
	\readfile($file);
	exit;
}

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
	$file = URL_DIR . $data['url'] .'.png';
	if(!\is_file($file)) {
		
		$image = new Image($url);
		$image->setOptions(['binary' => '/usr/local/bin/wkhtmltoimage', 'width' => 600, 'height' => 600]);

		if(!$image->saveAs($file)) {
			\errorLog('Could not get screenshot of '. $url);
			\header('Content-type: image/png');

			$w = 600;
			$h = 600;

			$img = @\imagecreatetruecolor($w, $h);
			if(!$img) {
				\readfile(URL_DIR . 'error.png');
				exit;
			}

			\imagesavealpha($img, true);
			$trans_colour = imagecolorallocatealpha($img, 0, 0, 0, 127);
    		\imagefill($img, 0, 0, $trans_colour);

			$font = 'Arial.ttf';
			$colour = \imagecolorallocate($img, 255, 255, 255);
			$text = \str_replace('http://', '', \base64_decode($data['url']));
			$size = 12;
			$angle = 0;

			$dimens = imagettfbbox($size, $angle, $font, $text);
			$x = ($w - $dimens[2])/2;
			$y = ($h - $dimens[3])/2;
			\imagettftext($img, $size, $angle, $x, $y, $colour, $font, $text);

			\imagepng($img);
			\imagedestroy($img);
			exit;
		}
		
		$image->send();
		exit;
	}

	\header('Content-type: image/png');
	\readfile($file);
} elseif ($type === TYPE_VIDEO) {
	\header('Content-type: image/jpeg');
	\readfile('http://img.youtube.com/vi/'. $filename .'/0.jpg');
}

// https://img.youtube.com/vi/cxLG2wtE7TM/0.jpg