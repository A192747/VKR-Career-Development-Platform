"""Create tables for topics, materials, and questions

Revision ID: 20250427
Revises: 
Create Date: 2025-04-27 00:00:00

"""
from alembic import op
import sqlalchemy as sa

revision = '20250427'
down_revision = None
branch_labels = None
depends_on = None

def upgrade():
    op.create_table('topics',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('name', sa.String(), nullable=False),
        sa.PrimaryKeyConstraint('id')
    )
    
    op.create_table('materials',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('content', sa.Text(), nullable=True),
        sa.Column('url', sa.String(), nullable=True),
        sa.PrimaryKeyConstraint('id')
    )
    
    op.create_table('questions',
        sa.Column('id', sa.Integer(), nullable=False),
        sa.Column('topic_id', sa.Integer(), nullable=False),
        sa.Column('question_text', sa.Text(), nullable=False),
        sa.Column('answer_text', sa.Text(), nullable=False),
        sa.Column('material_id', sa.Integer(), nullable=False),
        sa.ForeignKeyConstraint(['topic_id'], ['topics.id']),
        sa.ForeignKeyConstraint(['material_id'], ['materials.id']),
        sa.PrimaryKeyConstraint('id')
    )

def downgrade():
    op.drop_table('questions')
    op.drop_table('materials')
    op.drop_table('topics')