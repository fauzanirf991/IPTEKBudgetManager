<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

$response = array("error" => FALSE);

if (isset($_POST['username'])) {
    $username = $_POST['username'];

    $checkusername = $db->checkusername($username);
    if($checkusername != false){
        $response["error"] = FALSE;
        $response["user"]["username"] = $checkusername["email"];
        echo json_encode($response);
    }else{
        $response["error"] = TRUE;
        $response["error_msg"] = "Username yang anda masukan tidak terdaftar";
        echo json_encode($response);
    }
}else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Silahkan isi Username terlebih dahulu";
    echo json_encode($response);
}
?>