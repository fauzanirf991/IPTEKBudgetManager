<?php
require_once 'connect.php';

$dana = mysqli_query($conn, "SELECT (sum(total_harga) + sum(harga_aset)) AS ttl_pengeluaran FROM pengeluarankegiatan, aset");
while( $row = mysqli_fetch_assoc($dana) ){
        $ttlpengeluaran = $row['ttl_pengeluaran'];
        
}

echo "$ttlpengeluaran";



?>