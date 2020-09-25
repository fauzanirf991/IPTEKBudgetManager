<?php 

header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$key = $_POST['key'];
$id_pagudana     = $_POST['id_pagudana'];
$bukti = $_POST['bukti'];

if ( $key == "delete" ){

    $query = "TRUNCATE TABLE pagudana";
    $query2 = mysqli_query($conn, "TRUNCATE TABLE pagudanastatic");
    $query3 = mysqli_query($conn, "TRUNCATE TABLE kegiatan");
    $query3 = mysqli_query($conn, "TRUNCATE TABLE pengeluarankegiatan");
    $query3 = mysqli_query($conn, "TRUNCATE TABLE kas");
    $query3 = mysqli_query($conn, "TRUNCATE TABLE aset");


        if ( mysqli_query($conn, $query) ){

            $iparr = explode ("/", $bukti);
            $picture_split = $iparr[5];

            if ( unlink("buktipagudanapict/".$picture_split) ){

                $result["value"] = "1";
                $result["message"] = "Success!";

                echo json_encode($result);
                mysqli_close($conn);

            } else {
            
                $response["value"] = "0";
                $response["message"] = "Error to delete an image! ".mysqli_error($conn);
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

} else {
    $response["value"] = "0";
    $response["message"] = "Error! ".mysqli_error($conn);
    echo json_encode($response);

    mysqli_close($conn);
}

?>