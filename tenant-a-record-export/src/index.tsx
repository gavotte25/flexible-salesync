import React, { useEffect, useState } from 'react';
import { Download } from 'lucide-react';
import { utils, writeFile } from 'xlsx';

const {
  Button,
  Panel,
  RecordTable,
  Tooltip,
  TextInput,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
  LoadingSpinnerSmall
} = window.__miscCloudHostUI__;

type TenantProps = {
  realm: string;
  gatewayHost: string;
  token: string | null;
};

type OutputProperty = {
  id: string | null;
  property_name: string | null;
  property_label: string | null;
  item_value: string | null;
};

type OutputRecord = {
  id: string;
  name: string;
  properties: OutputProperty[];
};

type TypeItem = {
  id: string;
  name: string;
};

async function apiFetch(url: string, token: string | null, init?: RequestInit) {
  const res = await fetch(url, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers ?? {})
    }
  });
  if (!res.ok) {
    throw new Error(`Request failed: ${res.status}`);
  }
  return res.json();
}

function TabAction({ typeId }: { typeId?: string }) {
  const href = typeId ? `/private/csv-export?typeId=${typeId}` : '/private/csv-export';
  return (
    <>
      <a href={href}>
        <Button data-tooltip-id='export-csv' data-tooltip-content='Export CSV' className='space-x-2'>
          <Download size='1rem' />
          <p>Export CSV</p>
        </Button>
      </a>
      <Tooltip id='export-csv' />
    </>
  );
}

function CsvExportPage({ realm, gatewayHost, token }: TenantProps) {
  const [types, setTypes] = useState<TypeItem[]>([]);
  const [selectedTypeId, setSelectedTypeId] = useState<string | undefined>(
    new URLSearchParams(window.location.search).get('typeId') ?? undefined
  );
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [records, setRecords] = useState<OutputRecord[]>([]);
  const [properties, setProperties] = useState<unknown[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    apiFetch(`${gatewayHost}/type/${realm}/types`, token)
      .then(setTypes)
      .catch(() => setTypes([]));
  }, [gatewayHost, realm, token]);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(search), 500);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    if (!selectedTypeId) {
      setRecords([]);
      setProperties([]);
      return;
    }
    setIsLoading(true);
    Promise.all([
      apiFetch(`${gatewayHost}/type/${realm}/types/details/${selectedTypeId}`, token),
      apiFetch(`${gatewayHost}/record/${realm}/records/list`, token, {
        method: 'POST',
        body: JSON.stringify({
          type_id: selectedTypeId,
          search_term: debouncedSearch,
          is_asc: null,
          property_name: null,
          current_page: 1,
          page_size: 10000
        })
      })
    ])
      .then(([typeDetail, recordsResponse]) => {
        setProperties(typeDetail.properties ?? []);
        setRecords(recordsResponse.records ?? []);
      })
      .catch(() => {
        setProperties([]);
        setRecords([]);
      })
      .finally(() => setIsLoading(false));
  }, [gatewayHost, realm, token, selectedTypeId, debouncedSearch]);

  const selectedType = types.find((type) => type.id === selectedTypeId);

  const recordsQuery = {
    data: { records },
    error: null,
    isLoading,
    isRefetching: false,
    key: ['tenant-a-csv-export-records'],
    refetch: () => {}
  };
  const propertiesQuery = { data: { properties }, error: null, isLoading, key: ['tenant-a-csv-export-properties'] };

  const handleExport = () => {
    if (!selectedType) {
      return;
    }
    const rows = records.map((record) => {
      const row: Record<string, string> = { Name: record.name };
      record.properties.forEach((property) => {
        if (property.property_label) {
          row[property.property_label] = property.item_value ?? '';
        }
      });
      return row;
    });
    const worksheet = utils.json_to_sheet(rows);
    const workbook = utils.book_new();
    utils.book_append_sheet(workbook, worksheet, 'Data');
    const fileName = `${selectedType.name.toLowerCase()}-${new Date().toISOString().split('T')[0]}.csv`;
    writeFile(workbook, fileName);
  };

  return (
    <Panel className='m-4 mt-20 flex min-h-[calc(100dvh-96px)] flex-col overflow-auto p-4'>
      <section className='flex flex-col gap-4 pt-4'>
        <h1 className='text-[1.3rem]'>Export to CSV</h1>
        <div className='flex items-center gap-2'>
          <Select value={selectedTypeId} onValueChange={setSelectedTypeId}>
            <SelectTrigger className='w-64'>
              <SelectValue placeholder='Select a record type' />
            </SelectTrigger>
            <SelectContent>
              {types.map((type) => (
                <SelectItem key={type.id} value={type.id}>
                  {type.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {selectedType && (
            <>
              <TextInput
                value={search}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearch(e.target.value)}
                placeholder='Search records...'
                prefixIcon='search'
              />
              <Button intent='normal' zoom={false} className='space-x-2' onClick={handleExport} disabled={isLoading}>
                {isLoading ? <LoadingSpinnerSmall className='h-[1.2rem] w-[1.2rem]' /> : <Download size='1rem' />}
                <p>Export CSV</p>
              </Button>
            </>
          )}
        </div>
        {selectedType ? (
          <p className='text-sm text-muted-foreground'>
            {isLoading ? 'Loading...' : `${records.length} record(s) match your search.`}
          </p>
        ) : (
          <p className='text-sm text-muted-foreground'>Select a record type to preview and export its data.</p>
        )}
      </section>
      {selectedType && (
        <div className='-mx-4 mt-4 flex-grow'>
          <RecordTable typeId={selectedType.id} recordsQuery={recordsQuery} propertiesQuery={propertiesQuery} />
        </div>
      )}
    </Panel>
  );
}

window.__miscCloudCustomizations__ = window.__miscCloudCustomizations__ || {};
window.__miscCloudCustomizations__['record.tabs.actions'] = TabAction;
window.__miscCloudCustomizations__['record.csv-export'] = CsvExportPage;
