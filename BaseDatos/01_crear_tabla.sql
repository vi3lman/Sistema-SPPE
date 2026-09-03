CREATE TABLE transacciones (
    id_transaccion   VARCHAR(50) PRIMARY KEY,
    id_cliente       VARCHAR(30) NOT NULL,
    id_medio_pago    VARCHAR(100),
    monto            NUMERIC(10,2) NOT NULL,
    concepto         VARCHAR(150),
    estado           VARCHAR(20) NOT NULL,
    fecha_hora       VARCHAR(40),
    fecha_registro   TIMESTAMP NOT NULL DEFAULT now()
);