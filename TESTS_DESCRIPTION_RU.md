# Описание тестов
## AuthControllerIntegrationTest
- `register user`: проверяет успешную регистрацию пользователя.
- `login returns jwt`: проверяет успешный логин и возврат JWT.
- `login wrong password`: проверяет, что при неверном пароле возвращается `401`.
- `register invalid email`: проверяет валидацию email при регистрации (`400`).
- `register duplicate email`: проверяет, что повторная регистрация с тем же email возвращает `409`.
## GroupGraphQLIntegrationTest
- `create group and myGroups`: создание группы добавляет владельца в участники и группа видна в `myGroups`.
- `only owner adds members`: добавлять участников может только владелец группы.
- `groupMembers only for members`: список участников группы доступен только участникам.
- `groupDebts only for members`: долги группы доступны только участникам.
- `new member sees debts after join`: новый участник видит долги только после даты вступления.
- `income creates debts from current user`: групповая `INCOME` транзакция создает долги от текущего пользователя к остальным.
- `remove member keeps old debts`: удаление участника не удаляет его исторические долги.
- `cannot remove owner`: владельца нельзя удалить через `removeMember`.
- `non owner cannot delete group`: участник без прав владельца не может удалить группу.
- `createDebt for group members`: ручной долг можно создать только между участниками группы.
- `createDebt rejects outsider and self`: отклоняется долг для пользователя вне группы и долг самому себе.
- `owner creates debt between others`: владелец может создать долг между двумя другими участниками.
- `member creates debt only with self`: участник может создать долг только если сам является одной из сторон.
- `participant can delete debt`: участник долга может удалить этот долг.
- `non participant cannot delete debt`: участник группы, не участвующий в долге и не являющийся владельцем, не может удалить долг.
- `owner deletes any group debt`: владелец группы может удалить любой долг группы.
- `graphql input validation`: GraphQL-валидации отклоняют пустые и некорректные значения.
- `owner deletes group and debts`: удаление группы владельцем удаляет связанную группу и долги.
