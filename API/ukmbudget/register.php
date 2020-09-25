<?php
 
require_once 'include/DB_Functions.php';
$db = new DB_Functions();
 
// json response array
$response = array("error" => FALSE);
 
if (isset($_POST['nama']) && isset($_POST['username']) && isset($_POST['password'])) {
 
    // menerima parameter POST ( nama, username, password )
    $nama = $_POST['nama'];
    $username = $_POST['username'];
    $password = $_POST['password'];
    //cek jika user lebih dari satu
    if($db->checknumrow()){
        $response["error"] = TRUE;
        $response["error_msg"] = "Akses Dilarang (Sudah ada Bendahara yang mendaftar !)";
        echo json_encode($response);
    }else{
        // Cek jika user ada dengan username yang sama
        if ($db->isUserExisted($username)) {
            // user telah ada
            $response["error"] = TRUE;
            $response["error_msg"] = "User telah ada dengan username " . $username;
            echo json_encode($response);
        } else {
            // buat user baru
            $user = $db->simpanUser($nama, $username, $password);
            if ($user) {
                // simpan user berhasil
                $response["error"] = FALSE;
                $response["uid"] = $user["unique_id"];
                $response["user"]["nama"] = $user["nama"];
                $response["user"]["username"] = $user["email"];
                echo json_encode($response);
            } else {
                // gagal menyimpan user
                $response["error"] = TRUE;
                $response["error_msg"] = "Terjadi kesalahan saat melakukan registrasi";
                echo json_encode($response);
            }
        }
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Parameter (nama, username, atau password) ada yang kurang";
    echo json_encode($response);
}
?>