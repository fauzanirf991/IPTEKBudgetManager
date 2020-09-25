<?php 

header("Content-Type: application/json; charset=UTF-8");

require_once 'connect.php';

$key = $_POST['key'];
$id_pengeluaran      = $_POST['id_pengeluaran'];
$bukti = $_POST['bukti'];

if ( $key == "delete" ){
    $query1 = mysqli_query($conn, "SELECT total_harga FROM pengeluarankegiatan WHERE id_pengeluaran='$id_pengeluaran'");
    while($row = mysqli_fetch_assoc($query1)) {
    $total_harga = $row['total_harga'];
    }
    $dana = mysqli_query($conn, "SELECT jumlahdana FROM pagudana WHERE id_pagudana=1");
    while($row = mysqli_fetch_assoc($dana)) {
    $jumlahdana = $row['jumlahdana'];
    }
    $sisadana = $jumlahdana+$total_harga;

    $updatedana = mysqli_query($conn, "UPDATE pagudana SET jumlahdana='$sisadana'");


    $query = "DELETE FROM pengeluarankegiatan WHERE id_pengeluaran='$id_pengeluaran' ";

        if ( mysqli_query($conn, $query) ){

            $iparr = explode ("/", $bukti);
            $picture_split = $iparr[5];

            if ( unlink("notapict/".$picture_split) ){

                $result["value"] = "1";
                $result["message"] = "Success!";

                echo json_encode($result);
                mysqli_close($conn);

            } else {
            
                $response["value"] = "0";
                $response["message"] = "Error to delete a image! ".mysqli_error($conn);
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