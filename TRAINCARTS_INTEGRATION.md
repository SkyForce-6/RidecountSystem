# TrainCarts-Integration

Das Plugin arbeitet in zwei Modi.

## TrainCarts-Bridge

Wenn `Train_Carts` aktiv ist und die Event-Klasse `MemberBlockChangeEvent` vorhanden ist, registriert sich `TrainCartsApiBridge` per Reflection auf dieses Event. Dadurch wird die Zuggruppe aus TrainCarts gelesen und alle Minecarts der Gruppe werden nach Spielern durchsucht.

Auch wenn diese Bridge aktiv ist, bleibt der Bukkit-`VehicleMoveEvent`-Fallback als Reserve aktiv. Beide Wege teilen sich denselben Cooldown, damit derselbe Schildkontakt nicht doppelt gezaehlt wird.

## Bukkit-Fallback

Wenn TrainCarts nicht geladen ist oder die erwartete Event-Klasse nicht vorhanden ist, nutzt das Plugin `VehicleMoveEvent`. Dieser Weg ist bewusst einfach gehalten und reicht fuer Minecart-basierte Tests ohne harte TrainCarts-Compile-Dependency.

## Warum Reflection?

Fuer diese Testaufgabe soll das Plugin auch ohne lokale TrainCarts-API baubar bleiben. Deshalb ist `Train_Carts` in `plugin.yml` als `softdepend` eingetragen und die Bridge prueft die Klasse erst zur Laufzeit.

Eine produktive Weiterentwicklung koennte stattdessen eine direkte `compileOnly`-Dependency auf die konkrete TrainCarts-Version nutzen und eine native SignAction implementieren.
