<?php

$lat = urldecode($_POST['lat']);
$lng = urldecode($_POST['lng']);

/*
echo "{$lat}  ";
echo "{$lng}  ";
echo "";
echo "wait";*/

$db_host = "localhost";
$db_user = "root";
$db_passwd = "dlrm5538";
$db_name = "RyongDB";

$conn = mysqli_connect($db_host,$db_user,$db_passwd,$db_name);

if(mysqli_connect_errno($conn)){
echo "fail" . mysqli_connect_error();
}

$t1 = $lat - 0.00075;
$t2 = $lat + 0.00075;
$t3 = $lng - 0.00075;
$t4 = $lng + 0.00075;

$query = "select * from list where lat between ".$t1." and ".$t2." and lng between ".$t3." and ".$t4;

$res = mysqli_query($conn, $query);

if($res === null){
	echo "fail"; }

while($row = mysqli_fetch_array($res)) {
		echo "{$row[num]}/";
		echo "{$row[id]}/";
		echo "{$row[lat]}/";
		echo "{$row[lng]}/";
		echo "{$row[FoodPrice]}/";
		echo "{$row[FoodName]}/";
		echo "{$row[FoodComment]}/";
		echo "{$row[Location]}/";
		echo "{$row[BigOption]}/";
		echo "{$row[SmallOption]}/";
		echo "{$row[FilePath]}////";
	}
mysqli_close($conn);
?>

