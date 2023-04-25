# Vales de Salida.

El servicio agrupa la funcionalidad para controlar los movimientos, entradas y salidas, de art&iacute;culos. Estos viven
en el `Inventario Interno` de cada velatorio. El objetivo del servicio es que se pueda llevar dicho control
descontando del `stock` la cantidad del o de los art&iacute;culos cuando se genere el `Vale de Salida`, cuando este
se genere crear&aacute; un registro con los datos del `Velatorio` y de la `ODS` a la que se le asigne una 
`velaci&oacute;n a domicilio`


## Endpoints

| Endpoint                              | Par&aacute;metros | Registro en base de datos |
|---------------------------------------|-------------------|---------------------------|
| /mssivimss-vales-salida/vales-salida/ | N/A               | consultar-vales-salida    |
| /mssivimss-vales-salida/vales-salida/

