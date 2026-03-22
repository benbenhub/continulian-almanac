const SHANGHAI_TZ = 'Asia/Shanghai';

function getShanghaiDateTimeParts(date) {
    const formatter = new Intl.DateTimeFormat('zh-CN', {
        timeZone: SHANGHAI_TZ,
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
    const parts = formatter.formatToParts(date);
    const map = {};
    for (const p of parts) {
        if (p.type !== 'literal') map[p.type] = p.value;
    }
    const dateStr = `${map.year}-${map.month}-${map.day}`;
    const timeStr = `${map.hour}:${map.minute}`;
    return {
        year: map.year,
        month: map.month,
        day: map.day,
        hour: map.hour,
        minute: map.minute,
        second: map.second,
        dateStr,
        timeStr
    };
}

function getElementClass(element) {
    const map = {
        '木': 'element-wood',
        '火': 'element-fire',
        '土': 'element-earth',
        '金': 'element-metal',
        '水': 'element-water',
        '甲': 'element-wood', '乙': 'element-wood', '寅': 'element-wood', '卯': 'element-wood',
        '丙': 'element-fire', '丁': 'element-fire', '巳': 'element-fire', '午': 'element-fire',
        '戊': 'element-earth', '己': 'element-earth', '辰': 'element-earth', '戌': 'element-earth', '丑': 'element-earth', '未': 'element-earth',
        '庚': 'element-metal', '辛': 'element-metal', '申': 'element-metal', '酉': 'element-metal',
        '壬': 'element-water', '癸': 'element-water', '亥': 'element-water', '子': 'element-water'
    };
    return map[element] || '';
}

class TraditionalAstrologyWebsite {
    constructor() {
        this.defaultGender = '1';
        this.lastRealtimeKey = '';
        this.init();
    }

    init() {
        this.cacheElements();
        this.bindEvents();
        this.tick();
        setInterval(() => this.tick(), 1000);
        this.initBaziPage();
    }

    cacheElements() {
        this.beijingTimeEl = document.getElementById('beijing-time');
        this.lunarDateEl = document.getElementById('lunar-date');
        this.ganzhiEl = document.getElementById('ganzhi');
        this.almanacEl = document.getElementById('almanac');

        this.flipCards = {
            h1: document.getElementById('flip-h1'),
            h2: document.getElementById('flip-h2'),
            m1: document.getElementById('flip-m1'),
            m2: document.getElementById('flip-m2'),
            s1: document.getElementById('flip-s1'),
            s2: document.getElementById('flip-s2')
        };

        this.homeLunarEl = document.getElementById('home-lunar');
        this.homeAlmanacEl = document.getElementById('home-almanac');

        this.baziPageEl = document.getElementById('bazi-page');
        this.baziFormEl = document.getElementById('bazi-form');
        this.baziDateEl = document.getElementById('bazi-date');
        this.baziTimeEl = document.getElementById('bazi-time');
        this.baziGenderEl = document.getElementById('bazi-gender');
        this.baziSubmitEl = document.getElementById('bazi-submit');
        this.baziLoadingEl = document.getElementById('bazi-loading');
        this.baziErrorEl = document.getElementById('bazi-error');
        this.baziResultEl = document.getElementById('bazi-result');
        this.baziSummaryTableEl = document.getElementById('bazi-summary-table');
        this.baziGridEl = document.getElementById('bazi-grid');
        this.baziExtraEl = document.getElementById('bazi-extra');
        this.baziDayunEl = document.getElementById('bazi-dayun');
    }

    tick() {
        const now = new Date();
        const shanghai = getShanghaiDateTimeParts(now);
        this.updateBeijingTime(now);
        this.updateClock(shanghai);

        const key = `${shanghai.dateStr} ${shanghai.hour}:${shanghai.minute}`;
        const shouldUpdateHome = Boolean(this.homeLunarEl || this.homeAlmanacEl);
        if (shouldUpdateHome && key !== this.lastRealtimeKey) {
            this.lastRealtimeKey = key;
            this.updateRealtimeByApi(shanghai.dateStr, shanghai.timeStr);
        }
    }

    updateBeijingTime(date) {
        if (!this.beijingTimeEl) return;
        const timeString = date.toLocaleString('zh-CN', {
            timeZone: SHANGHAI_TZ,
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });
        this.beijingTimeEl.textContent = timeString;
    }

    updateClock(shanghai) {
        const digits = `${shanghai.hour}${shanghai.minute}${shanghai.second}`;
        this.updateFlipCard(this.flipCards.h1, digits[0]);
        this.updateFlipCard(this.flipCards.h2, digits[1]);
        this.updateFlipCard(this.flipCards.m1, digits[2]);
        this.updateFlipCard(this.flipCards.m2, digits[3]);
        this.updateFlipCard(this.flipCards.s1, digits[4]);
        this.updateFlipCard(this.flipCards.s2, digits[5]);
    }

    updateFlipCard(cardEl, nextValue) {
        if (!cardEl) return;
        const inner = cardEl.querySelector('.flip-card-inner');
        const front = cardEl.querySelector('.flip-front');
        const back = cardEl.querySelector('.flip-back');
        if (!inner || !front || !back) return;

        if (front.textContent === nextValue) return;

        if (inner.classList.contains('is-flipping')) {
            inner.classList.remove('is-flipping');
            front.textContent = back.textContent;
        }

        back.textContent = nextValue;
        inner.classList.add('is-flipping');

        const onEnd = () => {
            front.textContent = nextValue;
            inner.classList.remove('is-flipping');
        };
        inner.addEventListener('transitionend', onEnd, { once: true });
    }

    async updateRealtimeByApi(dateStr, timeStr) {
        try {
            const data = await this.fetchCalculate(dateStr, timeStr, this.defaultGender);

            if (this.homeLunarEl) {
                this.renderKeyValueList(this.homeLunarEl, [
                    { key: '公历', value: `${data.lunar.solarStr} ${data.lunar.week}` },
                    { key: '农历', value: `${data.lunar.yearName}年 ${data.lunar.monthName} ${data.lunar.dayName}` },
                    { key: '干支', value: `${data.lunar.yearGz}年 ${data.lunar.monthGz}月 ${data.lunar.dayGz}日 ${data.lunar.hourGz}时` },
                    { key: '生肖', value: data.lunar.zodiac },
                    { key: '节气', value: data.lunar.solarTerm }
                ]);
            }

            if (this.homeAlmanacEl) {
                this.renderKeyValueList(this.homeAlmanacEl, [
                    { key: '宜', value: data.almanac.yi },
                    { key: '忌', value: data.almanac.ji },
                    { key: '冲煞', value: `${data.almanac.chong} ${data.almanac.sha}` },
                    { key: '彭祖', value: data.almanac.pengzu },
                    { key: '吉神', value: data.almanac.jishen },
                    { key: '凶神', value: data.almanac.xiongshen },
                    { key: '胎神', value: data.almanac.taishen },
                    { key: '九星', value: data.almanac.jiuxing }
                ]);
            }

            if (this.lunarDateEl) {
                this.lunarDateEl.textContent = `${data.lunar.yearName}年 ${data.lunar.monthName} ${data.lunar.dayName}`;
            }
            if (this.ganzhiEl) {
                this.ganzhiEl.textContent = `${data.lunar.yearGz}年 ${data.lunar.monthGz}月 ${data.lunar.dayGz}日 ${data.lunar.hourGz}时`;
            }
            if (this.almanacEl) {
                this.almanacEl.textContent = `宜：${data.almanac.yi}；忌：${data.almanac.ji}`;
            }
        } catch (e) {
            if (this.homeLunarEl) {
                this.homeLunarEl.innerHTML = '';
            }
            if (this.homeAlmanacEl) {
                this.homeAlmanacEl.innerHTML = '';
            }
            if (this.almanacEl) this.almanacEl.textContent = '黄历加载失败';
        }
    }

    bindEvents() {
        const navLinks = document.querySelectorAll('.nav-menu a');
        navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                const href = (link.getAttribute('href') || '').trim();
                if (href && href !== '#') {
                    return;
                }
                e.preventDefault();
                navLinks.forEach(l => l.classList.remove('active'));
                link.classList.add('active');
            });
        });

    }

    async fetchCalculate(dateStr, timeStr, gender) {
        const url = `/api/calculate?date=${encodeURIComponent(dateStr)}&time=${encodeURIComponent(timeStr)}&gender=${encodeURIComponent(gender)}`;
        const res = await fetch(url);
        if (!res.ok) throw new Error(res.statusText || 'API Error');
        const data = await res.json();
        if (data.error) throw new Error(data.error);
        return data;
    }

    renderKeyValueList(container, items) {
        if (!container) return;
        container.innerHTML = '';
        for (const item of items) {
            const row = document.createElement('div');
            row.className = 'calc-item';

            const keyEl = document.createElement('div');
            keyEl.className = 'calc-item-key';
            keyEl.textContent = item.key;

            const valueEl = document.createElement('div');
            valueEl.className = 'calc-item-value';
            valueEl.textContent = item.value || '';

            row.appendChild(keyEl);
            row.appendChild(valueEl);
            container.appendChild(row);
        }
    }

    addBaziCell(text, className, extraClass) {
        const cell = document.createElement('div');
        cell.className = `bazi-cell${className ? ` ${className}` : ''}${extraClass ? ` ${extraClass}` : ''}`;
        if (typeof text === 'string') cell.textContent = text;
        return cell;
    }

    renderBaziGrid(pillars, container) {
        const root = container || this.baziGridEl;
        if (!root) return;
        root.innerHTML = '';

        root.appendChild(this.addBaziCell('', 'bazi-row-label'));
        const headers = ['年柱', '月柱', '日柱', '时柱'];
        for (const h of headers) {
            root.appendChild(this.addBaziCell(h, 'bazi-head'));
        }

        root.appendChild(this.addBaziCell('主星', 'bazi-row-label'));
        for (const p of pillars) {
            root.appendChild(this.addBaziCell(p.mainStar || '', 'bazi-sub'));
        }

        root.appendChild(this.addBaziCell('天干', 'bazi-row-label'));
        for (const p of pillars) {
            const c = this.addBaziCell(p.stem || '', 'bazi-stem', getElementClass(p.stemElement));
            root.appendChild(c);
        }

        root.appendChild(this.addBaziCell('地支', 'bazi-row-label'));
        for (const p of pillars) {
            const c = this.addBaziCell(p.branch || '', 'bazi-branch', getElementClass(p.branchElement));
            root.appendChild(c);
        }

        root.appendChild(this.addBaziCell('藏干', 'bazi-row-label'));
        for (const p of pillars) {
            const cell = this.addBaziCell('', 'bazi-meta');
            const wrap = document.createElement('div');
            wrap.className = 'bazi-hidden';
            for (const h of p.hiddenStems || []) {
                const s = document.createElement('span');
                s.className = getElementClass(h.element);
                s.textContent = h.stem;
                wrap.appendChild(s);
            }
            cell.appendChild(wrap);
            root.appendChild(cell);
        }

        root.appendChild(this.addBaziCell('副星', 'bazi-row-label'));
        for (const p of pillars) {
            const cell = this.addBaziCell('', 'bazi-meta');
            const wrap = document.createElement('div');
            wrap.className = 'bazi-hidden bazi-sub-star';
            for (const h of p.hiddenStems || []) {
                const s = document.createElement('span');
                s.textContent = h.star;
                wrap.appendChild(s);
            }
            cell.appendChild(wrap);
            root.appendChild(cell);
        }

        root.appendChild(this.addBaziCell('星运', 'bazi-row-label'));
        for (const p of pillars) {
            root.appendChild(this.addBaziCell(p.terrain || '', 'bazi-meta'));
        }

        root.appendChild(this.addBaziCell('纳音', 'bazi-row-label'));
        for (const p of pillars) {
            root.appendChild(this.addBaziCell(p.nayin || '', 'bazi-meta'));
        }

        root.appendChild(this.addBaziCell('空亡', 'bazi-row-label'));
        for (const p of pillars) {
            root.appendChild(this.addBaziCell(p.emptiness || '无', 'bazi-meta'));
        }
    }

    renderBaziExtra(bazi, container) {
        if (!container) return;
        container.innerHTML = `
            <div class="bazi-extra-item"><strong>胎元：</strong>${bazi.taiYuan || ''} (${bazi.taiYuanNayin || ''})</div>
            <div class="bazi-extra-item"><strong>胎息：</strong>${bazi.taiXi || ''} (${bazi.taiXiNayin || ''})</div>
            <div class="bazi-extra-item"><strong>命宫：</strong>${bazi.mingGong || ''} (${bazi.mingGongNayin || ''})</div>
            <div class="bazi-extra-item"><strong>身宫：</strong>${bazi.shenGong || ''} (${bazi.shenGongNayin || ''})</div>
            <div class="bazi-extra-item" style="flex: 1 1 100%; text-align: left; padding-left: 1rem;">
                <strong>起运：</strong>${bazi.childLimitText || ''}
            </div>
        `;
    }

    renderBaziDayun(dayuns, container) {
        if (!container || !dayuns || !dayuns.length) return;
        
        let html = '<h3 class="bazi-section-title">大运流年</h3><div class="bazi-dayun-grid">';
        
        for (const dy of dayuns) {
            html += `<div class="bazi-dayun-col">
                <div class="dy-header">
                    <div class="dy-star">${dy.mainStar}</div>
                    <div class="dy-name">
                        <span class="${getElementClass(dy.stem)}">${dy.stem}</span><span class="${getElementClass(dy.branch)}">${dy.branch}</span>
                    </div>
                    <div class="dy-age">${dy.startAge}岁</div>
                    <div class="dy-year">${dy.startYear}年</div>
                </div>
                <div class="dy-liunians">`;
            
            for (const ln of dy.liunians || []) {
                const lnStem = ln.name.charAt(0);
                const lnBranch = ln.name.charAt(1);
                html += `<div class="dy-liunian-item">
                    <span class="ln-year">${ln.year}</span>
                    <span class="ln-name"><span class="${getElementClass(lnStem)}">${lnStem}</span><span class="${getElementClass(lnBranch)}">${lnBranch}</span></span>
                    <span class="ln-age">${ln.age}岁</span>
                </div>`;
            }
            
            html += `</div></div>`;
        }
        html += '</div>';
        container.innerHTML = html;
    }

    initBaziPage() {
        if (!this.baziPageEl || !this.baziFormEl) return;

        const now = getShanghaiDateTimeParts(new Date());
        const params = new URLSearchParams(window.location.search);
        const dateStr = params.get('date') || now.dateStr;
        const timeStr = params.get('time') || now.timeStr;
        const gender = params.get('gender') || this.defaultGender;

        if (this.baziDateEl) this.baziDateEl.value = dateStr;
        if (this.baziTimeEl) this.baziTimeEl.value = timeStr;
        if (this.baziGenderEl) this.baziGenderEl.value = gender;

        this.defaultGender = gender;

        this.baziFormEl.addEventListener('submit', (e) => {
            e.preventDefault();
            const d = this.baziDateEl?.value || '';
            const t = this.baziTimeEl?.value || '';
            const g = this.baziGenderEl?.value || this.defaultGender;
            if (!d || !t) return;
            this.defaultGender = g;
            this.calculateBaziAndRender(d, t, g);
        });

        this.calculateBaziAndRender(dateStr, timeStr, gender);
    }

    setBaziState({ loading, error, resultVisible }) {
        if (this.baziLoadingEl) this.baziLoadingEl.hidden = !loading;
        if (this.baziErrorEl) {
            this.baziErrorEl.hidden = !error;
            if (error) this.baziErrorEl.textContent = error;
        }
        if (this.baziResultEl) this.baziResultEl.hidden = !resultVisible;
        if (this.baziSubmitEl) this.baziSubmitEl.disabled = loading;
    }

    async calculateBaziAndRender(dateStr, timeStr, gender) {
        this.setBaziState({ loading: true, error: '', resultVisible: false });
        try {
            const data = await this.fetchCalculate(dateStr, timeStr, gender);
            if (this.baziSummaryTableEl) {
                this.baziSummaryTableEl.innerHTML = `
                    <div class="bazi-summary-item">
                        <span class="bazi-summary-label">公历：</span>
                        <span class="bazi-summary-value">${data.lunar.solarStr} ${data.lunar.week}</span>
                    </div>
                    <div class="bazi-summary-item">
                        <span class="bazi-summary-label">农历：</span>
                        <span class="bazi-summary-value">${data.lunar.yearName}年 ${data.lunar.monthName} ${data.lunar.dayName}</span>
                    </div>
                    <div class="bazi-summary-item">
                        <span class="bazi-summary-label">性别：</span>
                        <span class="bazi-summary-value">${gender === '1' ? '乾造 (男)' : '坤造 (女)'}</span>
                    </div>
                    <div class="bazi-summary-item">
                        <span class="bazi-summary-label">生肖：</span>
                        <span class="bazi-summary-value">${data.lunar.zodiac}</span>
                    </div>
                `;
            }
            this.renderBaziGrid(data.bazi?.pillars || [], this.baziGridEl);
            this.renderBaziExtra(data.bazi || {}, this.baziExtraEl);
            this.renderBaziDayun(data.bazi?.dayuns || [], this.baziDayunEl);
            this.setBaziState({ loading: false, error: '', resultVisible: true });
        } catch (e) {
            this.setBaziState({ loading: false, error: `排盘失败：${e.message || e}`, resultVisible: false });
        }
    }

}

document.addEventListener('DOMContentLoaded', () => {
    new TraditionalAstrologyWebsite();
});
