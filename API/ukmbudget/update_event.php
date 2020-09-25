<?php
header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$key = $_POST['key'];

if ( $key == "update" ){

    $id_kegiatan        = $_POST['id_kegiatan'];
    $nama_kegiatan      = $_POST['nama_kegiatan'];
    $tgl_kegiatan       = $_POST['tgl_kegiatan'];

    
    $tgl_kegiatan =  date('Y-m-d', strtotime($tgl_kegiatan));

    $query = "UPDATE kegiatan SET 
    nama_kegiatan='$nama_kegiatan',  
    tgl_kegiatan='$tgl_kegiatan'
    WHERE id_kegiatan='$id_kegiatan' ";

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



?>