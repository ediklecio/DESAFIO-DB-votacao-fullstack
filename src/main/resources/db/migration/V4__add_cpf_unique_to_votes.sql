ALTER TABLE votes
    ADD COLUMN cpf VARCHAR(11) NOT NULL;

-- RN01 extended: a CPF votes at most once per agenda too, regardless of the
-- member_id used to submit it - mirrors uk_votes_agenda_member below.
ALTER TABLE votes
    ADD CONSTRAINT uk_votes_agenda_cpf UNIQUE (agenda_id, cpf);
