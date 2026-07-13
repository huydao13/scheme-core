import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10  },
    { duration: '1m',  target: 50  },
    { duration: '1m',  target: 100 },
    { duration: '30s', target: 0   },
  ],
  thresholds: {
    http_req_duration: ['p(95)<100'],  // merchant resolve phải < 100ms
    http_req_failed:   ['rate<0.01'],
  },
};

const TERMINAL_CODES = ['TRM-001', 'TRM-002', 'TRM-003'];

export default function () {
  const code = TERMINAL_CODES[Math.floor(Math.random() * TERMINAL_CODES.length)];

  const r = http.get(
      `http://103.90.226.48:8084/v1/merchants/resolve?terminalCode=${code}`,
      { headers: { 'X-Internal-Service': 'order-service' } }
  );

  check(r, {
    'status 200': (r) => r.status === 200,
    '< 100ms':    (r) => r.timings.duration < 100,
  });

  sleep(0.5);
}
