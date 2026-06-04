"""商品详情图集顺序(DEBT-004):首张=主图(sort_order 最小),读侧按 sort_order 排。"""


def test_detail_images_ordered_by_sort_order(client, conn):
    # 给商品 1 加一张更小 sort_order(应排最前)和一张更大(应排最后)
    conn.execute("INSERT INTO product_images (product_id, url, sort_order) VALUES (1, '/uploads/main.png', -5)")
    conn.execute("INSERT INTO product_images (product_id, url, sort_order) VALUES (1, '/uploads/last.png', 99)")
    conn.commit()
    images = client.get("/api/products/1", headers={"X-Device-Id": "d"}).get_json()["images"]
    assert images[0] == "/uploads/main.png"   # 首张 = sort_order 最小 = 主图
    assert images[-1] == "/uploads/last.png"   # sort_order 最大排最后
