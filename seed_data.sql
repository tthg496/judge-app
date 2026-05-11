-- === SEED DATA FOR JUDGEAPP DATABASE ===

-- Insert Problems
INSERT INTO problems (title, content, time_limit, memory_limit) 
VALUES (
    'Sum of Two Numbers',
    'Cho hai số nguyên a và b. Hãy tính tổng của chúng.

Input:
Hai dòng, mỗi dòng một số nguyên.

Output:
Một số nguyên là tổng của a và b.',
    1.0,
    256
);

INSERT INTO problems (title, content, time_limit, memory_limit) 
VALUES (
    'Fibonacci Number',
    'Cho số tự nhiên n. Hãy tính số Fibonacci thứ n.

Fibonacci: F(1)=1, F(2)=1, F(n)=F(n-1)+F(n-2) với n>2

Input:
Một số tự nhiên n (1 <= n <= 40)

Output:
Số Fibonacci thứ n',
    2.0,
    256
);

INSERT INTO problems (title, content, time_limit, memory_limit) 
VALUES (
    'Sort Array',
    'Cho mảng n phần tử. Hãy sắp xếp mảng theo thứ tự tăng dần.

Input:
Dòng đầu: số n
Dòng tiếp: n số nguyên

Output:
Mảng đã sắp xếp, các phần tử cách nhau bởi dấu cách',
    1.5,
    256
);

INSERT INTO problems (title, content, time_limit, memory_limit) 
VALUES (
    'Check Palindrome',
    'Kiểm tra xem một chuỗi có phải là palindrome hay không.

Palindrome là chuỗi đọc xuôi và đọc ngược giống nhau.

Input:
Một chuỗi

Output:
YES nếu là palindrome, NO nếu không',
    1.0,
    256
);

INSERT INTO problems (title, content, time_limit, memory_limit) 
VALUES (
    'Count Primes',
    'Cho số n. Hãy đếm có bao nhiêu số nguyên tố nhỏ hơn n.

Input:
Một số n (n <= 1000000)

Output:
Số lượng số nguyên tố nhỏ hơn n',
    3.0,
    256
);

-- === TESTCASES FOR PROBLEM 1: Sum of Two Numbers ===
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (1, '5
3', '8', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (1, '10
20', '30', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (1, '100
200', '300', 0);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (1, '-5
3', '-2', 0);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (1, '1000000
2000000', '3000000', 0);

-- === TESTCASES FOR PROBLEM 2: Fibonacci ===
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (2, '1', '1', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (2, '5', '5', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (2, '10', '55', 0);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (2, '20', '6765', 0);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (2, '30', '832040', 0);

-- === TESTCASES FOR PROBLEM 3: Sort Array ===
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (3, '5
5 2 8 1 9', '1 2 5 8 9', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (3, '3
3 2 1', '1 2 3', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (3, '1
5', '5', 0);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (3, '6
6 5 4 3 2 1', '1 2 3 4 5 6', 0);

-- === TESTCASES FOR PROBLEM 4: Palindrome ===
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (4, 'racecar', 'YES', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (4, 'hello', 'NO', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (4, 'a', 'YES', 0);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (4, 'aba', 'YES', 0);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (4, 'abc', 'NO', 0);

-- === TESTCASES FOR PROBLEM 5: Count Primes ===
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (5, '10', '4', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (5, '2', '0', 1);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (5, '100', '25', 0);
INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (5, '1000', '168', 0);
