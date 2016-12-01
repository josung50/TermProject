<?php

$_id = $_POST['_id'];
$_lat = urldecode($_POST['_lat']);
$_lng = urldecode($_POST['_lng']);
$_FoodPrice = urldecode($_POST['_FoodPrice']);
$_FoodName = urldecode($_POST['_FoodName']);
$_FoodComment = urldecode($_POST['_FoodComment']);//감사합니다 urldecode님... 정말 사랑해요
$_Location = urldecode($_POST['_Location']);
$_BigOption = urldecode($_POST['_BigOption']);
$_SmallOption = urldecode($_POST['_SmallOption']);

echo "{$_id}  ";
echo "{$_lat}  ";
echo "{$_lng}	";
echo "{$_FoodPrice}  ";
echo "{$_FoodName}  ";
echo "{$_FoodComment}  ";
echo "{$_Location}  ";
echo "{$_BigOption}  ";
echo "{$_SmallOption}  ";
echo "";

echo "Waiting for data to insert!";



$db_host = "localhost";
$db_user = "root";
$db_passwd = "dlrm5538";
$db_name = "RyongDB";

$conn = mysqli_connect($db_host,$db_user,$db_passwd,$db_name);
mysql_set_charset("utf8",$conn);


if(mysqli_connect_errno($conn)){
echo "fail" . mysqli_connect_error();
}
if($_id){
echo "success";
}

mysqli_query($conn,"INSERT INTO list (num, id, lat, lng, FoodPrice, FoodName, FoodComment, Location, BigOption, SmallOption) VALUES ('','$_id','$_lat','$_lng','$_FoodPrice','$_FoodName','$_FoodComment','$_Location','$_BigOption','$_SmallOption')");

mysqli_close($conn);
?>

