<?php
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
 
// json response array
$response = array("error" => FALSE);
 
if (isset($_POST['username']) && isset($_POST['password']) && isset($_POST['usertype'])) {
 
    // menerima parameter POST ( username dan password )
    $username = $_POST['username'];
    $password = $_POST['password'];
    $usertype = $_POST['usertype'];
 
    // get the user by username and password
    // get user berdasarkan username dan password
    $user = $db->getUserByUsernameAndPassword($username, $password, $usertype);
 
    if ($user != false) {
        // user ditemukan
        $response["error"] = FALSE;
        $response["uid"] = $user["unique_id"];
        $response["user"]["id"] = $user["id"];
        $response["user"]["nama"] = $user["nama"];
        $response["user"]["username"] = $user["email"];
        echo json_encode($response);
    } else {
        // user tidak ditemukan password/username salah
        $response["error"] = TRUE;
        $response["error_msg"] = "Login gagal. Password/Username salah";
        echo json_encode($response);
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Parameter (username atau password) ada yang kurang";
    echo json_encode($response);
}
?>