ALTER TABLE sessao ADD COLUMN processada BOOLEAN DEFAULT FALSE;
CREATE INDEX idx_sessao_processada_data ON sessao(processada, data_fechamento);