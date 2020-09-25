<?php

require_once 'connect.php';

$key = $_POST['key'];

$nama_kegiatan = $_POST['nama_kegiatan'];
$tgl_kegiatan = $_POST['tgl_kegiatan'];


if ( $key == "insert" ){

    $tgl_kegiatan = date('Y-m-d', strtotime($tgl_kegiatan));

    $query = "INSERT INTO kegiatan(nama_kegiatan, tgl_kegiatan) 
    VALUES ('$nama_kegiatan', '$tgl_kegiatan') ";

        if ( mysqli_query($conn, $query) ){

            $result["value"] = "1";
            $result["message"] = "Success!";

            echo json_encode($result);
            mysqli_close($conn);
        } 
        else {
            $response["value"] = "0";
            $response["message"] = "Error! ".mysqli_error($conn);
            echo json_encode($response);

            mysqli_close($conn);
        }
}
else {
    $response["value"] = "0";
    $response["message"] = "Error! ".mysqli_error($conn);
    echo json_encode($response);

    mysqli_close($conn);
}
?>


