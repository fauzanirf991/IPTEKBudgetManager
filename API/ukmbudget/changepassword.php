<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();

$response = array("error" => FALSE);

if (isset($_POST['username']) && isset($_POST['password'])){
    $username = $_POST['username'];
    $password = $_POST['password'];

    $cgpassword = $db->changePassword($username, $password);

    if($cgpassword){
        $response["error"] = FALSE;
        echo json_encode($response);
    }else{
        $response["error"] = TRUE;
        $response["error_msg"] = "Mengubah password gagal!";
        echo json_encode($response);
    }
}else{
    $response["error"] = TRUE;
    $response["error_msg"] = "Mengubah password gagal!";
}