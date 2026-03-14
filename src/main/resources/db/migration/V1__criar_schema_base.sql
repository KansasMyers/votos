CREATE TABLE pauta (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sessao (
    id BIGSERIAL PRIMARY KEY,
    pauta_id BIGINT NOT NULL UNIQUE,
    data_abertura TIMESTAMP NOT NULL,
    data_fechamento TIMESTAMP NOT NULL,
    CONSTRAINT fk_sessao_pauta FOREIGN KEY (pauta_id) REFERENCES pauta(id)
);

CREATE TABLE voto (
    id BIGSERIAL PRIMARY KEY,
    pauta_id BIGINT NOT NULL,
    associado_id VARCHAR(255) NOT NULL,
    voto_escolha VARCHAR(3) NOT NULL,
    data_voto TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_voto_pauta FOREIGN KEY (pauta_id) REFERENCES pauta(id),
    CONSTRAINT uk_voto_associado_pauta UNIQUE (pauta_id, associado_id) -- 1 voto por associado/pauta
);

CREATE INDEX idx_voto_pauta ON voto(pauta_id);