-- Demo crew. Dose budgets are PLACEHOLDERS (60 mSv per mission) until Arsen sources real values.
INSERT INTO crew (id, name, role, skills, dose_budget_micro_sv) VALUES
    ('c1', 'Alex Rivera', 'Commander',       'EVA,PILOT',      60000),
    ('c2', 'Maya Chen',   'Flight Engineer', 'EVA,ENGINEER',   60000),
    ('c3', 'Omar Haddad', 'Medical Officer', 'MEDIC',          60000),
    ('c4', 'Lena Novak',  'Scientist',       'SCIENCE,EVA',    60000);
