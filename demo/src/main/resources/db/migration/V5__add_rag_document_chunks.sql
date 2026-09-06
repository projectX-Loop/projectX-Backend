CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE rag_document_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_key VARCHAR(255) NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    embedding vector(${rag_embedding_dimensions}) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_rag_document_chunk_document_chunk UNIQUE (document_key, chunk_index)
);

CREATE INDEX idx_rag_document_chunk_embedding_hnsw
    ON rag_document_chunk USING hnsw (embedding vector_cosine_ops);
